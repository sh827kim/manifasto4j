package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Sqrt - 제곱근
 * 예: sqrt(get("data.value"))
 */
public record Sqrt(ExprNode arg) implements ExprNode {

    public Sqrt {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Sqrt of(ExprNode arg) {
        return new Sqrt(arg);
    }
}
