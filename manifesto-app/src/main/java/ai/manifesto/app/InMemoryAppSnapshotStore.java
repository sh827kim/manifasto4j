package ai.manifesto.app;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.utils.SnapshotStoreUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryAppSnapshotStore - 프로세스 내 session snapshot 저장소
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
