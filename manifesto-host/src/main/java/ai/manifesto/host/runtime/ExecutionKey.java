package ai.manifesto.host.runtime;

import java.util.Objects;

/**
 * KR: mailbox/runner 라우팅에 사용하는 실행 식별자 래퍼입니다.
 * EN: Opaque execution identifier used for mailbox/runner routing.
 */
public record ExecutionKey(String value) {
    public ExecutionKey {
        Objects.requireNonNull(value, "value is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    public static ExecutionKey fromIntentId(String intentId) {
        return new ExecutionKey(intentId);
    }
}
