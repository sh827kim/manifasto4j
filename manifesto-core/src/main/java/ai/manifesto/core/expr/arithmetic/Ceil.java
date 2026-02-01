package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Ceil - 올림
 * 예: ceil(get("data.value"))
 */
public record Ceil(ExprNode arg) implements ExprNode {

    public Ceil {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Ceil of(ExprNode arg) {
        return new Ceil(arg);
    }
}
