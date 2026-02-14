package ai.manifesto.app;

import ai.manifesto.core.*;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.HostRuntime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("App 최소 동작 테스트")
class AppTest {

    @Test
    @DisplayName("act 실행 후 subscribe 알림")
    void testActAndSubscribe() throws Exception {
        FlowNode effectFlow = FlowNode.Seq.of(
            FlowNode.Patch.set("status", new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Halt.of("done")
        );

        ActionSpec effectAction = new ActionSpec.Builder("notify")
            .flow(effectFlow)
            .build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test",
            "1.0.0",
            new ActionSpec[] { effectAction },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime();

        App app = AppFactory.createApp(schema, snapshot, host);
        app.ready();

        AtomicReference<Object> latest = new AtomicReference<>();
        app.subscribe(s -> s.getData().get("status"), latest::set);

        Intent intent = new Intent("notify", new HashMap<>(), UUID.randomUUID().toString());
        ActionHandle handle = app.act(intent);

        assertTrue(
            handle.getStatus() == ComputeStatus.HALTED || handle.getStatus() == ComputeStatus.COMPLETE
        );
        assertEquals(ActionPhase.COMPLETED, handle.getPhase());
        assertTrue(handle.getUpdates().size() >= 3);
        assertEquals(ActionPhase.PREPARING, handle.getUpdates().get(0).phase());
        assertEquals(ActionPhase.EXECUTING, handle.getUpdates().get(1).phase());
        assertEquals("ok", latest.get());
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

        DomainSchema.Builder builder = new DomainSchema.Builder(id, version)
            .hash(hash);
        for (ActionSpec action : actions) {
            builder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            builder.addDataField(field);
        }
        return builder.build();
    }
}
