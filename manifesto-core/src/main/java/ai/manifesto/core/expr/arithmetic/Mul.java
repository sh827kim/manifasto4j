package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Mul는 Core 표현식 엔진에서 전달되는 mul 데이터를 담는 불변 레코드입니다.
 * EN: Mul is an immutable record carrying mul data in the Core expression engine.
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
