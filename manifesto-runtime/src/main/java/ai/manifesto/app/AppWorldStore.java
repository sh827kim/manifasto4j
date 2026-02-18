package ai.manifesto.app;

import ai.manifesto.core.Snapshot;
import ai.manifesto.world.schema.WorldId;

import java.util.List;

/**
 * KR: App 레벨 branch/world snapshot 저장소 경계 계약입니다.
 * EN: App-level repository boundary for branch/world snapshot persistence.
 */
public interface AppWorldStore {
    void save(String branchName, WorldId worldId, Snapshot snapshot);

    BranchState load(String branchName);

    List<String> listBranchNames();

    record BranchState(String branchName, WorldId worldId, Snapshot snapshot) {
    }
}
