package ai.manifesto.host.runtime;

import java.util.Objects;

/**
 * KR: ExecutionKey는 Host 런타임 계층에서 전달되는 execution key 데이터를 담는 불변 레코드입니다.
 * EN: ExecutionKey is an immutable record carrying execution key data in the Host runtime layer.
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
