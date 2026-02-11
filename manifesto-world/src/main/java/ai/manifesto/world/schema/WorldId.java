package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: WorldId는 World 도메인 식별자를 타입 안전하게 표현하는 값 객체입니다.
 * EN: WorldId is a value object that strongly types a World-domain identifier.
 */
public final class WorldId {
    private final String value;

    private WorldId(String value) {
        this.value = Objects.requireNonNull(value, "value is required");
    }

    public static WorldId of(String value) {
        return new WorldId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldId worldId)) return false;
        return Objects.equals(value, worldId.value);
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
