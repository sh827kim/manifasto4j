package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Mul - 곱셈 (*)
 * 예: mul(get("data.price"), lit(1.1))
 */
public record Mul(ExprNode left, ExprNode right) implements ExprNode {

    public Mul {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Mul of(ExprNode left, ExprNode right) {
        return new Mul(left, right);
    }
}
