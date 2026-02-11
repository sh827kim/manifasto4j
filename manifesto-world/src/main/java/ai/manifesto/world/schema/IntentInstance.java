package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: IntentInstance는 World 스키마 계층에서 intent instance 역할을 수행하는 구현 타입입니다.
 * EN: IntentInstance is an implementation type performing intent instance roles in the World schema layer.
 */
public final class IntentInstance {
    private final IntentBody body;
    private final String intentId;
    private final String intentKey;
    private final IntentMeta meta;

    public IntentInstance(IntentBody body, String intentId, String intentKey, IntentMeta meta) {
        this.body = Objects.requireNonNull(body, "body is required");
        this.intentId = Objects.requireNonNull(intentId, "intentId is required");
        this.intentKey = Objects.requireNonNull(intentKey, "intentKey is required");
        this.meta = Objects.requireNonNull(meta, "meta is required");
    }

    public IntentBody getBody() {
        return body;
    }

    public String getIntentId() {
        return intentId;
    }

    public String getIntentKey() {
        return intentKey;
    }

    public IntentMeta getMeta() {
        return meta;
    }
}
