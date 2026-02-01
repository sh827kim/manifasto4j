package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Neg - 부호 반전
 * 예: neg(get("data.balance"))
 */
public record Neg(ExprNode arg) implements ExprNode {

    public Neg {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Neg of(ExprNode arg) {
        return new Neg(arg);
    }
}
