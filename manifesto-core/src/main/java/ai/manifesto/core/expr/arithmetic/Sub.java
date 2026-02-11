package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Sub는 Core 표현식 엔진에서 전달되는 sub 데이터를 담는 불변 레코드입니다.
 * EN: Sub is an immutable record carrying sub data in the Core expression engine.
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
