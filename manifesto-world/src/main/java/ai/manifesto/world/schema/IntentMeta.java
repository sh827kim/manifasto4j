package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: IntentMeta는 World 스키마 계층에서 intent meta 역할을 수행하는 구현 타입입니다.
 * EN: IntentMeta is an implementation type performing intent meta roles in the World schema layer.
 */
public final class IntentMeta {
    private final IntentOrigin origin;

    public IntentMeta(IntentOrigin origin) {
        this.origin = Objects.requireNonNull(origin, "origin is required");
    }

    public IntentOrigin getOrigin() {
        return origin;
    }
}
