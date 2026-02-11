package ai.manifesto.host;

import ai.manifesto.core.Patch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * KR: EffectResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: EffectResult is a result type carrying operation or execution outcomes.
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
