package ai.manifesto.app;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.comparison.Eq;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
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
                new Eq(new Get("data.status"), new Lit("")),
                FlowNode.Effect.of("host.bump", Map.of()),
                FlowNode.Halt.of("done")
        );
        ActionSpec action = new ActionSpec.Builder("bump").flow(effectFlow).build();

        DomainSchema schema = buildSchemaWithHash(
                "urn:test:world-app",
                "1.0.0",
                new ActionSpec[]{action},
                new FieldSpec[]{new FieldSpec("status", "string", false, "")}
        );

        Snapshot snapshot = Snapshot.builder()
                .data(new HashMap<>(Map.of("status", "")))
                .computed(new HashMap<>())
                .system(SystemState.initial())
                .input(new HashMap<>())
                .meta(Snapshot.SnapshotMeta.create(0, 100L, "seed", schema.getHash()))
                .build();

        HostRuntime host = new HostRuntime().register("host.bump", params ->
                EffectResult.of(List.of(ai.manifesto.core.Patch.set("data.status", "ok")))
        );

        App app = AppFactory.createWorldApp(schema, snapshot, host, "human-1", ActorKind.HUMAN);
        app.ready();

        var first = app.act(new Intent("bump", Map.of(), UUID.randomUUID().toString()));
        assertEquals(ai.manifesto.core.ComputeStatus.COMPLETE, first.getStatus());

        var world = app.getWorld();
        assertNotNull(world);
        assertTrue(world.getStore().listWorlds().size() >= 1);
        assertEquals(1, world.getProposalQueue().size());
        assertTrue(world.getProposalQueue().getTerminal().size() >= 1);

        WorldId genesisId = world.getStore().getGenesis().getWorldId();
        app.switchBranch(genesisId);
        assertEquals("", app.getSnapshot().getData().get("status"));
    }

    private DomainSchema buildSchemaWithHash(
            String id,
            String version,
            ActionSpec[] actions,
            FieldSpec[] dataFields
    ) {
        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(id, version);
        for (ActionSpec action : actions) {
            tempBuilder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            tempBuilder.addDataField(field);
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
        return builder.build();
    }
}
