package ai.manifesto.core.schema;

import java.util.List;
import java.util.Objects;

/**
 * DomainMeta - 도메인 메타 정보
 */
public final class DomainMeta {
    private final String name;
    private final String description;
    private final List<String> authors;

    public DomainMeta(String name, String description, List<String> authors) {
        this.name = name;
        this.description = description;
        this.authors = authors != null ? List.copyOf(authors) : null;
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
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", authors=" + (authors != null ? authors.size() : null) +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainMeta that)) return false;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(authors, that.authors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, authors);
    }
}
