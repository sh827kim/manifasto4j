package ai.manifesto.app;

import ai.manifesto.bridge.BridgeRuntime;
import ai.manifesto.bridge.ProjectionResult;
import ai.manifesto.bridge.SourceEvent;
import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.HostRuntime;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppBridgeIntegrationTest {

    @Test
    void bridgeRoutesEventToIntentAndAppExecutes() throws Exception {
        ActionSpec setFromUi = new ActionSpec.Builder("setFromUi")
            .addInputField("value", FieldSpec.required("value", "string"))
            .flow(FlowNode.Patch.set("status", new Get("input.value")))
            .build();
        ActionSpec setFromApi = new ActionSpec.Builder("setFromApi")
            .addInputField("value", FieldSpec.required("value", "string"))
            .flow(FlowNode.Patch.set("status", new Get("input.value")))
            .build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test:app-bridge",
            "1.0.0",
            new ActionSpec[] { setFromUi, setFromApi },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 100L, "seed", schema.getHash()))
            .build();

        App app = AppFactory.createApp(schema, snapshot, new HostRuntime());
        app.ready();

        BridgeRuntime bridge = new BridgeRuntime(
            Map.of(
                SourceEvent.Kind.UI,
                (event, view) -> ProjectionResult.intent(
                    new Intent("setFromUi", Map.of("value", "ui"), UUID.randomUUID().toString())
                ),
                SourceEvent.Kind.API,
                (event, view) -> ProjectionResult.intent(
                    new Intent("setFromApi", Map.of("value", "api"), UUID.randomUUID().toString())
                )
            ),
            null
        );

        Intent uiIntent = bridge.project(new SourceEvent(SourceEvent.Kind.UI, "evt-ui", Map.of(), null), app.getSnapshot());
        app.act(uiIntent);
        assertEquals("ui", app.getSnapshot().getData().get("status"));

        Intent apiIntent = bridge.project(new SourceEvent(SourceEvent.Kind.API, "evt-api", Map.of(), null), app.getSnapshot());
        app.act(apiIntent);
        assertEquals("api", app.getSnapshot().getData().get("status"));
    }

    @Test
    void appSessionStoreRestoresLatestSnapshot() throws Exception {
        ActionSpec setStatus = new ActionSpec.Builder("setStatus")
            .addInputField("value", FieldSpec.required("value", "string"))
            .flow(FlowNode.Patch.set("status", new Get("input.value")))
            .build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test:app-session",
            "1.0.0",
            new ActionSpec[] { setStatus },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 100L, "seed", schema.getHash()))
            .build();

        InMemoryAppSnapshotStore store = new InMemoryAppSnapshotStore();
        String sessionId = "session-1";

        App first = AppFactory.createApp(schema, snapshot, new HostRuntime(), sessionId, store);
        first.ready();
        first.act(new Intent("setStatus", Map.of("value", "persisted"), UUID.randomUUID().toString()));
        assertEquals("persisted", first.getSnapshot().getData().get("status"));

        App second = AppFactory.createApp(schema, snapshot, new HostRuntime(), sessionId, store);
        second.ready();
        assertEquals("persisted", second.getSnapshot().getData().get("status"));
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
