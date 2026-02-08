package ai.manifesto.world.schema;

import java.util.Objects;

public final class QuorumRule {
    private final QuorumKind kind;
    private final Integer count;

    private QuorumRule(QuorumKind kind, Integer count) {
        this.kind = Objects.requireNonNull(kind, "kind is required");
        this.count = count;
    }

    public static QuorumRule unanimous() {
        return new QuorumRule(QuorumKind.UNANIMOUS, null);
    }

    public static QuorumRule majority() {
        return new QuorumRule(QuorumKind.MAJORITY, null);
    }

    public static QuorumRule threshold(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than zero");
        }
        return new QuorumRule(QuorumKind.THRESHOLD, count);
    }

    public QuorumKind getKind() {
        return kind;
    }

    public Integer getCount() {
        return count;
    }
}
