package ai.manifesto.core.schema;

import java.util.Map;
import java.util.Objects;

/**
 * TypeSpec - named type definition (v0.3.3)
 *
 * Minimal representation: definition is a canonical map.
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
