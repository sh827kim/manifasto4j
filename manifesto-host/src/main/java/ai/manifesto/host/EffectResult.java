package ai.manifesto.host;

import ai.manifesto.core.Patch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * EffectResult - Effect 실행 결과로 생성된 Patch 목록
 */
public final class EffectResult {
    private final List<Patch> patches;

    public EffectResult(List<Patch> patches) {
        Objects.requireNonNull(patches, "patches is required");
        this.patches = List.copyOf(patches);
    }

    public List<Patch> getPatches() {
        return new ArrayList<>(patches);
    }

    public static EffectResult of(List<Patch> patches) {
        return new EffectResult(patches);
    }
}
