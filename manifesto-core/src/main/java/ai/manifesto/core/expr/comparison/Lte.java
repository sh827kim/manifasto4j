package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Lte - 이하 비교 (<=)
 * 예: lte(get("data.priority"), lit(3))
 */
public record Lte(ExprNode left, ExprNode right) implements ExprNode {

    public Lte {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Lte of(ExprNode left, ExprNode right) {
        return new Lte(left, right);
    }
}
