package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Gt - 초과 비교 (>)
 * 예: gt(get("data.count"), lit(10))
 */
public record Gt(ExprNode left, ExprNode right) implements ExprNode {

    public Gt {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Gt of(ExprNode left, ExprNode right) {
        return new Gt(left, right);
    }
}
