package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Sub - 뺄셈 (-)
 * 예: sub(get("data.count"), lit(1))
 */
public record Sub(ExprNode left, ExprNode right) implements ExprNode {

    public Sub {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Sub of(ExprNode left, ExprNode right) {
        return new Sub(left, right);
    }
}
