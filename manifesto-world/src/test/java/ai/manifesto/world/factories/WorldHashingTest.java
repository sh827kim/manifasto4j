package ai.manifesto.world.factories;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Requirement;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.utils.CanonicalUtils;
import ai.manifesto.core.utils.HashUtils;
import ai.manifesto.world.schema.WorldId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class WorldHashingTest {

    @Test
    void excludesAllDollarPrefixedNamespacesFromHash() {
        Snapshot snapshot1 = snapshot(
                mapOf(
                        "count", 42,
                        "$host", mapOf("internal", true),
                        "$mel", mapOf("guard", "a"),
                        "$app", mapOf("future", true),
                        "$trace", mapOf("debug", true)
                ),
                List.of(),
                null,
                List.of()
        );

        Snapshot snapshot2 = snapshot(
                mapOf(
                        "count", 42,
                        "$host", mapOf("internal", false, "x", 1),
                        "$mel", mapOf("guard", "b"),
                        "$app", mapOf("future", false),
                        "$trace", mapOf("debug", false)
                ),
                List.of(),
                null,
                List.of()
        );

        assertEquals(WorldHashing.computeSnapshotHash(snapshot1), WorldHashing.computeSnapshotHash(snapshot2));
    }

    @Test
    void changesHashWhenDomainDataChanges() {
        Snapshot snapshot1 = snapshot(mapOf("count", 42), List.of(), null, List.of());
        Snapshot snapshot2 = snapshot(mapOf("count", 43), List.of(), null, List.of());

        assertNotEquals(WorldHashing.computeSnapshotHash(snapshot1), WorldHashing.computeSnapshotHash(snapshot2));
    }

    @Test
    void ignoresErrorMessageAndTimestampAndOrder() {
        ErrorValue errorA1 = ErrorValue.create("E-1", "message-a", "act-1", "node-1", 1000L);
        ErrorValue errorA2 = ErrorValue.create("E-1", "message-b", "act-1", "node-1", 2000L);

        ErrorValue errorB = ErrorValue.create("E-2", "message-c", "act-2", "node-2", 3000L);

        Snapshot snapshot1 = snapshot(
                mapOf("x", 1),
                List.of(errorA1, errorB),
                null,
                List.of()
        );

        Snapshot snapshot2 = snapshot(
                mapOf("x", 1),
                List.of(errorB, errorA2),
                null,
                List.of()
        );

        assertEquals(WorldHashing.computeSnapshotHash(snapshot1), WorldHashing.computeSnapshotHash(snapshot2));
    }

    @Test
    void terminalStatusUsesLastErrorOrPending() {
        Snapshot completed = snapshot(mapOf("x", 1), List.of(), null, List.of());
        Snapshot failedByLastError = snapshot(
                mapOf("x", 1),
                List.of(),
                ErrorValue.create("E", "m", "a", "n", 1L),
                List.of()
        );
        Snapshot failedByPending = snapshot(
                mapOf("x", 1),
                List.of(),
                null,
                List.of(Requirement.builder().id("req-1").type("effect").createdAt(1L).build())
        );

        String completedHash = WorldHashing.computeSnapshotHash(completed);
        String failedByLastErrorHash = WorldHashing.computeSnapshotHash(failedByLastError);
        String failedByPendingHash = WorldHashing.computeSnapshotHash(failedByPending);

        assertNotEquals(completedHash, failedByLastErrorHash);
        assertNotEquals(completedHash, failedByPendingHash);
    }

    @Test
    void pendingDigestDependsOnSortedPendingIds() {
        Requirement req1 = Requirement.builder().id("req-a").type("effect").createdAt(1L).build();
        Requirement req2 = Requirement.builder().id("req-b").type("effect").createdAt(2L).build();

        Snapshot snapshot1 = snapshot(mapOf("x", 1), List.of(), null, List.of(req1, req2));
        Snapshot snapshot2 = snapshot(mapOf("x", 1), List.of(), null, List.of(req2, req1));
        Snapshot snapshot3 = snapshot(mapOf("x", 1), List.of(), null, List.of(req1));

        assertEquals(WorldHashing.computeSnapshotHash(snapshot1), WorldHashing.computeSnapshotHash(snapshot2));
        assertNotEquals(WorldHashing.computeSnapshotHash(snapshot1), WorldHashing.computeSnapshotHash(snapshot3));
    }

    @Test
    void computesWorldIdUsingCanonicalObjectHash() {
        String schemaHash = "schema-hash";
        String snapshotHash = "snapshot-hash";

        WorldId worldId = WorldHashing.computeWorldId(schemaHash, snapshotHash);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("schemaHash", schemaHash);
        input.put("snapshotHash", snapshotHash);
        String expected = HashUtils.sha256(CanonicalUtils.toCanonical(input));

        assertEquals(expected, worldId.value());
    }

    private static Snapshot snapshot(
            Map<String, Object> data,
            List<ErrorValue> errors,
            ErrorValue lastError,
            List<Requirement> pendingRequirements
    ) {
        SystemState system = SystemState.of(
                SystemState.Status.IDLE,
                lastError,
                errors,
                pendingRequirements,
                null
        );

        Snapshot.SnapshotMeta meta = Snapshot.SnapshotMeta.create(1, 1000L, "seed", "schema-hash");
        return Snapshot.builder()
                .data(data)
                .computed(Map.of())
                .system(system)
                .input(Map.of())
                .meta(meta)
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
