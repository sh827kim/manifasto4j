package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Lt - 미만 비교 (<)
 * 예: lt(get("data.count"), lit(100))
 */
public record Lt(ExprNode left, ExprNode right) implements ExprNode {

    public Lt {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Lt of(ExprNode left, ExprNode right) {
        return new Lt(left, right);
    }
}
