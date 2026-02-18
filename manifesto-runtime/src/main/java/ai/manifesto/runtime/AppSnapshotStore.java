package ai.manifesto.runtime;

import ai.manifesto.core.Snapshot;

/**
 * KR: AppSnapshotStore는 App snapshot 저장/조회 경계를 정의하는 저장소 인터페이스입니다.
 * EN: AppSnapshotStore is the repository interface defining App snapshot save/load boundaries.
 */
public interface AppSnapshotStore {
    Snapshot load(String sessionId);
    void save(String sessionId, Snapshot snapshot);
}
