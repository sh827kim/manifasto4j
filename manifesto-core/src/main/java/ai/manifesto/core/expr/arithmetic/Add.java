package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Add - 덧셈 (+)
 * 예: add(get("data.count"), lit(1))
 */
public record Add(ExprNode left, ExprNode right) implements ExprNode {

    public Add {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Add of(ExprNode left, ExprNode right) {
        return new Add(left, right);
    }
}
