package ai.manifesto.runtime;

import ai.manifesto.core.ComputeStatus;
import ai.manifesto.core.Intent;
import ai.manifesto.core.Patch;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.comparison.Eq;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.EffectResult;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.WorldId;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrossModuleIntegrationRegressionTest {

    @Test
    void approvedActionAdvancesCoreHostWorldAndAppState() throws Exception {
        DomainSchema schema = buildTodoSchema();
        Snapshot initialSnapshot = baseSnapshot(schema, "");

        HostRuntime host = new HostRuntime().register("host.bump", params ->
            EffectResult.of(List.of(Patch.set("status", "done")))
        );
        App app = AppFactory.createWorldApp(schema, initialSnapshot, host, "human-1", ActorKind.HUMAN);
        app.ready();

        var handle = app.act(new Intent("bump", Map.of(), UUID.randomUUID().toString()));

        assertEquals(ComputeStatus.COMPLETE, handle.getStatus());
        assertEquals("done", app.getSnapshot().getData().get("status"));
        assertTrue(handle.getUpdates().stream().anyMatch(update -> update.phase() == ActionPhase.SUBMITTED));
        assertTrue(handle.getUpdates().stream().anyMatch(update -> update.phase() == ActionPhase.COMPLETED));
        assertTrue(app.getWorld().getProposalQueue().getTerminal().size() >= 1);
        assertTrue(app.getWorld().listEdges(null).size() >= 1);
    }

    @Test
    void branchSwitchRestoresSnapshotAndAllowsIndependentContinuation() throws Exception {
        DomainSchema schema = buildTodoSchema();
        Snapshot initialSnapshot = baseSnapshot(schema, "");

        HostRuntime host = new HostRuntime().register("host.bump", params ->
            EffectResult.of(List.of(Patch.set("status", "done")))
        );
        App app = AppFactory.createWorldApp(schema, initialSnapshot, host, "human-1", ActorKind.HUMAN);
        app.ready();

        app.act(new Intent("bump", Map.of(), UUID.randomUUID().toString()));
        assertEquals("done", app.getSnapshot().getData().get("status"));

        WorldId genesisId = app.getWorld().getStore().getGenesis().getWorldId();
        app.createBranch("genesis", genesisId);
        app.switchBranch("genesis");
        assertEquals("", app.getSnapshot().getData().get("status"));

        app.act(new Intent("bump", Map.of(), UUID.randomUUID().toString()));
        assertEquals("done", app.getSnapshot().getData().get("status"));
        assertEquals("genesis", app.getCurrentBranchName());
    }

    @Test
    void policyRejectedActionStopsBeforeWorldSubmission() throws Exception {
        DomainSchema schema = buildTodoSchema();
        Snapshot initialSnapshot = baseSnapshot(schema, "");
        HostRuntime host = new HostRuntime().register("host.bump", params ->
            EffectResult.of(List.of(Patch.set("status", "done")))
        );

        App worldApp = AppFactory.createWorldApp(schema, initialSnapshot, host, "human-1", ActorKind.HUMAN);
        var configured = AppFactory.createApp(new AppConfig(
            schema,
            initialSnapshot,
            host,
            worldApp.getWorld(),
            worldApp.getWorld().getRegisteredActors().get(0),
            null,
            null,
            (intent, snapshot) -> AppPolicyService.PolicyDecision.reject("blocked-by-test"),
            new InMemoryAppWorldStore()
        ));
        configured.ready();

        int terminalBefore = configured.getWorld().getProposalQueue().getTerminal().size();
        var handle = configured.act(new Intent("bump", Map.of(), UUID.randomUUID().toString()));

        assertEquals(ActionPhase.REJECTED, handle.getPhase());
        assertEquals(ComputeStatus.ERROR, handle.getStatus());
        assertEquals("", configured.getSnapshot().getData().get("status"));
        assertEquals(terminalBefore, configured.getWorld().getProposalQueue().getTerminal().size());
    }

    private DomainSchema buildTodoSchema() {
        FlowNode flow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("")),
            FlowNode.Effect.of("host.bump", Map.of()),
            FlowNode.Halt.of("done")
        );
        ActionSpec action = new ActionSpec.Builder("bump").flow(flow).build();
        FieldSpec statusField = new FieldSpec("status", "string", false, "");
        ComputedFieldDef marker = ComputedFieldDef.simple("computed.marker", new Lit("ok"));

        DomainSchema temp = new DomainSchema.Builder("urn:test:cross-module", "1.0.0")
            .addAction(action)
            .addDataField(statusField)
            .addComputedField(marker)
            .hash("")
            .build();
        String hash = ValidationUtils.computeSchemaHash(temp);

        return new DomainSchema.Builder("urn:test:cross-module", "1.0.0")
            .addAction(action)
            .addDataField(statusField)
            .addComputedField(marker)
            .hash(hash)
            .build();
    }

    private Snapshot baseSnapshot(DomainSchema schema, String status) {
        return Snapshot.builder()
            .data(new HashMap<>(Map.of("status", status)))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 100L, "seed", schema.getHash()))
            .build();
    }
}
