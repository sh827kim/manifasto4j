package ai.manifesto.app;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.utils.SnapshotStoreUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KR: InMemoryAppSnapshotStore는 App snapshot을 메모리에 보관하는 저장소 구현입니다.
 * EN: InMemoryAppSnapshotStore is an in-memory snapshot store implementation for App.
 */
public final class InMemoryAppSnapshotStore implements AppSnapshotStore {
    private final Map<String, Snapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public Snapshot load(String sessionId) {
        Objects.requireNonNull(sessionId, "sessionId is required");
        return SnapshotStoreUtils.deepCopySnapshot(snapshots.get(sessionId));
    }

    @Override
    public void save(String sessionId, Snapshot snapshot) {
        Objects.requireNonNull(sessionId, "sessionId is required");
        Objects.requireNonNull(snapshot, "snapshot is required");
        snapshots.put(sessionId, SnapshotStoreUtils.canonicalizeForStorage(snapshot));
    }
}
