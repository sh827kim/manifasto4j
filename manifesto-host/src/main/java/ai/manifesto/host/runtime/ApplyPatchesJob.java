package ai.manifesto.host.runtime;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Patch;
import ai.manifesto.core.Snapshot;

import java.util.List;
import java.util.Objects;

/**
 * KR: Fulfill 이후 patch 집합을 apply 단계에서 반영하기 위한 job입니다.
 * EN: Job for applying collected patches in a dedicated apply phase after fulfill.
 */
public final class ApplyPatchesJob implements HostJob {
    private final Snapshot baseSnapshot;
    private final List<Patch> patches;
    private final Intent intent;

    public ApplyPatchesJob(Snapshot baseSnapshot, List<Patch> patches, Intent intent) {
        this.baseSnapshot = Objects.requireNonNull(baseSnapshot, "baseSnapshot is required");
        this.patches = List.copyOf(Objects.requireNonNull(patches, "patches is required"));
        this.intent = Objects.requireNonNull(intent, "intent is required");
    }

    public Snapshot getBaseSnapshot() {
        return baseSnapshot;
    }

    public List<Patch> getPatches() {
        return patches;
    }

    public Intent getIntent() {
        return intent;
    }

    @Override
    public HostJobType getType() {
        return HostJobType.APPLY_PATCHES;
    }
}
