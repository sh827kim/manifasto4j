package ai.manifesto.app;

import ai.manifesto.core.Snapshot;

/**
 * AppSnapshotStore - session 기반 snapshot 저장소 추상화
 */
public interface AppSnapshotStore {
    Snapshot load(String sessionId);
    void save(String sessionId, Snapshot snapshot);
}
