package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Round - 반올림
 * 예: round(get("data.value"))
 */
public record Round(ExprNode arg) implements ExprNode {

    public Round {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Round of(ExprNode arg) {
        return new Round(arg);
    }
}
