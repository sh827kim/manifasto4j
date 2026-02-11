package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Div는 Core 표현식 엔진에서 전달되는 div 데이터를 담는 불변 레코드입니다.
 * EN: Div is an immutable record carrying div data in the Core expression engine.
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
