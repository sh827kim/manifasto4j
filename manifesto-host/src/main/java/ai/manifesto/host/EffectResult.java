package ai.manifesto.host;

import ai.manifesto.core.Patch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * KR: Effect 실행 결과를 Patch 목록으로 캡슐화하는 값 객체입니다.
 * EN: Value object that carries patches produced by effect execution.
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
