package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Gte - 이상 비교 (>=)
 * 예: gte(get("data.age"), lit(18))
 */
public record Gte(ExprNode left, ExprNode right) implements ExprNode {

    public Gte {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Gte of(ExprNode left, ExprNode right) {
        return new Gte(left, right);
    }
}
