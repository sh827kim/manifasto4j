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
}
