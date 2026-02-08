package ai.manifesto.world.schema;

import java.util.Objects;

public final class ProposalId {
    private final String value;

    private ProposalId(String value) {
        this.value = Objects.requireNonNull(value, "value is required");
    }

    public static ProposalId of(String value) {
        return new ProposalId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProposalId that)) return false;
        return Objects.equals(value, that.value);
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
