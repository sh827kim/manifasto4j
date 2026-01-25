package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Div - 나눗셈 (/)
 * 0으로 나누면 null 반환 (예외 던지지 않음)
 * 예: div(get("data.total"), get("data.count"))
 */
public record Div(ExprNode left, ExprNode right) implements ExprNode {

    public Div {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Div of(ExprNode left, ExprNode right) {
        return new Div(left, right);
    }
}
