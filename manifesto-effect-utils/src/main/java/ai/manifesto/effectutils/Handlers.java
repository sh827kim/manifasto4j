package ai.manifesto.effectutils;

import ai.manifesto.core.Patch;
import ai.manifesto.host.EffectHandler;
import ai.manifesto.host.EffectResult;

import java.util.List;
import java.util.Objects;

/**
 * Handlers - effect handler 유틸리티 (최소 구현)
 */
public final class Handlers {
    private Handlers() {}

    public static EffectHandler empty() {
        return input -> new EffectResult(List.of());
    }

    public static EffectHandler constant(List<Patch> patches) {
        Objects.requireNonNull(patches, "patches is required");
        return input -> new EffectResult(patches);
    }
}
