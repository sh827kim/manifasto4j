package ai.manifesto.bridge;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BridgeRuntime projection 테스트")
class BridgeRuntimeTest {

    @Test
    @DisplayName("SourceEvent → Intent projection")
    void testProjection() {
        Projection projection = (event, view) ->
            new Intent("test", Map.of("value", view.data().get("value")), "intent-1");

        BridgeRuntime runtime = new BridgeRuntime(projection);

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("value", 7)))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();

        SourceEvent event = new SourceEvent(SourceEvent.Kind.API, "evt-1", Map.of(), null);
        Intent intent = runtime.project(event, snapshot);

        assertEquals("test", intent.getType());
        assertEquals(7, ((Map<?, ?>) intent.getInput()).get("value"));
    }

    @Test
    @DisplayName("event kind별 라우팅 projection 적용")
    void testRoutedProjection() {
        Projection uiProjection = (event, view) -> new Intent("ui.intent", Map.of(), "intent-ui");
        Projection apiProjection = (event, view) -> new Intent("api.intent", Map.of(), "intent-api");
        Projection fallback = (event, view) -> new Intent("fallback.intent", Map.of(), "intent-fallback");

        BridgeRuntime runtime = new BridgeRuntime(
            Map.of(
                SourceEvent.Kind.UI, uiProjection,
                SourceEvent.Kind.API, apiProjection
            ),
            fallback
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("value", 7)))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();

        Intent uiIntent = runtime.project(new SourceEvent(SourceEvent.Kind.UI, "e1", Map.of(), null), snapshot);
        Intent apiIntent = runtime.project(new SourceEvent(SourceEvent.Kind.API, "e2", Map.of(), null), snapshot);
        Intent agentIntent = runtime.project(new SourceEvent(SourceEvent.Kind.AGENT, "e3", Map.of(), null), snapshot);

        assertEquals("ui.intent", uiIntent.getType());
        assertEquals("api.intent", apiIntent.getType());
        assertEquals("fallback.intent", agentIntent.getType());
    }
}
