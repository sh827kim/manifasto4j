package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: EdgeId는 World 도메인 식별자를 타입 안전하게 표현하는 값 객체입니다.
 * EN: EdgeId is a value object that strongly types a World-domain identifier.
 */
public final class EdgeId {
    private final String value;

    private EdgeId(String value) {
        this.value = Objects.requireNonNull(value, "value is required");
    }

    public static EdgeId of(String value) {
        return new EdgeId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeId edgeId)) return false;
        return Objects.equals(value, edgeId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
