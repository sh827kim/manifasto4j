package ai.manifesto.runtime;

import ai.manifesto.core.Intent;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppWorldIntegrationTest {

    @Test
    void worldEnabledAppExecutesThroughWorldAndSupportsBranchSwitch() throws Exception {
        FlowNode effectFlow = FlowNode.If.of(
                new Eq(new Get("status"), new Lit("")),
                FlowNode.Effect.of("host.bump", Map.of()),
                FlowNode.Halt.of("done")
        );
        ActionSpec action = new ActionSpec.Builder("bump").flow(effectFlow).build();

        DomainSchema schema = buildSchemaWithHash(
                "urn:test:world-app",
                "1.0.0",
                new ActionSpec[]{action},
                new FieldSpec[]{new FieldSpec("status", "string", false, "")},
                new ComputedFieldDef[]{
                        ComputedFieldDef.simple("computed.genesisFlag", new Lit("ok"))
                }
        );

        Snapshot snapshot = Snapshot.builder()
                .data(new HashMap<>(Map.of("status", "")))
                .computed(new HashMap<>())
                .system(SystemState.initial())
                .input(new HashMap<>())
                .meta(Snapshot.SnapshotMeta.create(0, 100L, "seed", schema.getHash()))
                .build();

        HostRuntime host = new HostRuntime().register("host.bump", params ->
                EffectResult.of(List.of(ai.manifesto.core.Patch.set("status", "ok")))
        );

        App app = AppFactory.createWorldApp(schema, snapshot, host, "human-1", ActorKind.HUMAN);
        app.ready();

        var first = app.act(new Intent("bump", Map.of(), UUID.randomUUID().toString()));
        assertEquals(ai.manifesto.core.ComputeStatus.COMPLETE, first.getStatus());
        assertEquals(ActionPhase.COMPLETED, first.getPhase());
        assertTrue(first.getUpdates().stream().anyMatch(update -> update.phase() == ActionPhase.SUBMITTED));
        assertEquals("ok", app.getSnapshot().getData().get("status"));

        var world = app.getWorld();
        assertNotNull(world);
        assertTrue(world.getStore().listWorlds().size() >= 1);
        assertEquals(1, world.getProposalQueue().size());
        assertTrue(world.getProposalQueue().getTerminal().size() >= 1);
        assertEquals(first.getResult().getSnapshot().getMeta().getSchemaHash(), app.getSnapshot().getMeta().getSchemaHash());
        assertTrue(app.getCurrentBranchId() != null);
        assertTrue(app.listBranches().size() >= 1);
        assertEquals("main", app.getCurrentBranchName());
        app.createBranch("genesis", world.getStore().getGenesis().getWorldId());
        assertTrue(app.listBranchNames().contains("genesis"));
        assertTrue(app.getHeads().size() >= 2);
        assertNotNull(app.getLatestHead());
        assertEquals("main", app.getLatestHead().branchName());

        WorldId genesisId = world.getStore().getGenesis().getWorldId();
        app.switchBranch("genesis");
        assertEquals("", app.getSnapshot().getData().get("status"));
        assertEquals(genesisId, app.getCurrentBranchId());
    }

    @Test
    void worldReadyEvaluatesComputedAtGenesis() throws Exception {
        DomainSchema schema = buildSchemaWithHash(
                "urn:test:world-ready8",
                "1.0.0",
                new ActionSpec[]{new ActionSpec.Builder("noop").flow(FlowNode.Halt.of("done")).build()},
                new FieldSpec[]{new FieldSpec("status", "string", false, "")},
                new ComputedFieldDef[]{
                        ComputedFieldDef.simple("computed.genesisValue", new Lit(42))
                }
        );

        Snapshot snapshot = Snapshot.builder()
                .data(new HashMap<>(Map.of("status", "")))
                .computed(new HashMap<>())
                .system(SystemState.initial())
                .input(new HashMap<>())
                .meta(Snapshot.SnapshotMeta.create(0, 100L, "seed", schema.getHash()))
                .build();

        App app = AppFactory.createWorldApp(schema, snapshot, new HostRuntime(), "human-1", ActorKind.HUMAN);
        app.ready();

        assertEquals(42, app.getSnapshot().getComputed().get("computed.genesisValue"));
    }

    private DomainSchema buildSchemaWithHash(
            String id,
            String version,
            ActionSpec[] actions,
            FieldSpec[] dataFields,
            ComputedFieldDef[] computedFields
    ) {
        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(id, version);
        for (ActionSpec action : actions) {
            tempBuilder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            tempBuilder.addDataField(field);
        }
        for (ComputedFieldDef computedField : computedFields) {
            tempBuilder.addComputedField(computedField);
        }
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        DomainSchema.Builder builder = new DomainSchema.Builder(id, version).hash(hash);
        for (ActionSpec action : actions) {
            builder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            builder.addDataField(field);
        }
        for (ComputedFieldDef computedField : computedFields) {
            builder.addComputedField(computedField);
        }
        return builder.build();
    }
}
