package ai.manifesto.core.schema;

import java.util.List;
import java.util.Objects;

/**
 * KR: DomainMeta는 Core 스키마 계층에서 domain meta 역할을 수행하는 구현 타입입니다.
 * EN: DomainMeta is an implementation type performing domain meta roles in the Core schema layer.
 */
public final class DomainMeta {
    private final String namespace;
    private final String name;
    private final String description;
    private final List<String> authors;

    public DomainMeta(String name, String description, List<String> authors) {
        this(null, name, description, authors);
    }

    public DomainMeta(String namespace, String name, String description, List<String> authors) {
        this.namespace = namespace;
        this.name = name;
        this.description = description;
        this.authors = authors != null ? List.copyOf(authors) : null;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    @Override
    public String toString() {
        return "DomainMeta{" +
               "namespace='" + namespace + '\'' +
               ", " +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", authors=" + (authors != null ? authors.size() : null) +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainMeta that)) return false;
        return Objects.equals(namespace, that.namespace) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(authors, that.authors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name, description, authors);
    }
}
