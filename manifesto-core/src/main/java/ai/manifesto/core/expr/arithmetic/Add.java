package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Add는 Core 표현식 엔진에서 전달되는 add 데이터를 담는 불변 레코드입니다.
 * EN: Add is an immutable record carrying add data in the Core expression engine.
 */
public record Add(ExprNode left, ExprNode right) implements ExprNode {

    public Add {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Add of(ExprNode left, ExprNode right) {
        return new Add(left, right);
    }
}
