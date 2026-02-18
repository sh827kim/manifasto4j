package ai.manifesto.app;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryAppSnapshotStoreTest {

    @Test
    void saveFiltersPlatformNamespacesAndLoadReturnsDefensiveCopy() {
        InMemoryAppSnapshotStore store = new InMemoryAppSnapshotStore();
        Snapshot snapshot = snapshot(
            mapOf(
                "count", 1,
                "$host", mapOf("currentIntentId", "intent-1"),
                "$mel", mapOf("guards", mapOf("intent", mapOf("g1", "intent-1")))
            )
        );

        store.save("session-1", snapshot);

        Snapshot loaded1 = store.load("session-1");
        assertNotNull(loaded1);
        assertEquals(1, loaded1.getData().get("count"));
        assertFalse(loaded1.getData().containsKey("$host"));
        assertFalse(loaded1.getData().containsKey("$mel"));

        Map<String, Object> mutatedData = loaded1.getData();
        mutatedData.put("count", 999);

        Snapshot loaded2 = store.load("session-1");
        assertEquals(1, loaded2.getData().get("count"));
    }

    private static Snapshot snapshot(Map<String, Object> data) {
        return Snapshot.builder()
            .data(data)
            .computed(Map.of())
            .system(SystemState.initial())
            .input(Map.of())
            .meta(Snapshot.SnapshotMeta.create(1, 1L, "seed", "schema-hash"))
            .build();
    }

    private static Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            result.put((String) values[i], values[i + 1]);
        }
        return result;
    }
}
