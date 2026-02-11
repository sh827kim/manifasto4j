package ai.manifesto.core.schema;

import java.util.Map;
import java.util.Objects;

/**
 * KR: TypeSpec는 Core 스키마 계층에서 type spec 역할을 수행하는 구현 타입입니다.
 * EN: TypeSpec is an implementation type performing type spec roles in the Core schema layer.
 */
public final class TypeSpec {
    private final String name;
    private final Map<String, Object> definition;

    public TypeSpec(String name, Map<String, Object> definition) {
        this.name = Objects.requireNonNull(name, "name is required");
        this.definition = definition != null ? Map.copyOf(definition) : Map.of();
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "TypeSpec{" +
               "name='" + name + '\'' +
               ", definitionKeys=" + definition.keySet() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeSpec that)) return false;
        return Objects.equals(name, that.name) &&
               Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, definition);
    }
}
