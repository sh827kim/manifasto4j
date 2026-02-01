package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Abs - 절댓값
 * 예: abs(get("data.score"))
 */
public record Abs(ExprNode arg) implements ExprNode {

    public Abs {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Abs of(ExprNode arg) {
        return new Abs(arg);
    }
}
