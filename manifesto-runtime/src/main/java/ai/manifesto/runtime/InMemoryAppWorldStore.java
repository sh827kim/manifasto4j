package ai.manifesto.runtime;

import ai.manifesto.core.Snapshot;
import ai.manifesto.world.schema.WorldId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: branch 이름 기준으로 world/snapshot 상태를 저장하는 in-memory 저장소입니다.
 * EN: In-memory repository storing world/snapshot state keyed by branch name.
 */
public final class InMemoryAppWorldStore implements AppWorldStore {
    private final Map<String, BranchState> storage = new LinkedHashMap<>();

    @Override
    public synchronized void save(String branchName, WorldId worldId, Snapshot snapshot) {
        String safeBranch = normalizeBranch(branchName);
        storage.put(safeBranch, new BranchState(safeBranch, worldId, snapshot));
    }

    @Override
    public synchronized BranchState load(String branchName) {
        return storage.get(normalizeBranch(branchName));
    }

    @Override
    public synchronized List<String> listBranchNames() {
        return List.copyOf(storage.keySet());
    }

    private String normalizeBranch(String branchName) {
        String safe = Objects.requireNonNull(branchName, "branchName is required").trim();
        if (safe.isBlank()) {
            throw new IllegalArgumentException("branchName must not be blank");
        }
        return safe;
    }
}
