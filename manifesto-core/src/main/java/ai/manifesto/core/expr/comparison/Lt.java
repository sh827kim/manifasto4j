package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Lt는 Core 표현식 엔진에서 전달되는 lt 데이터를 담는 불변 레코드입니다.
 * EN: Lt is an immutable record carrying lt data in the Core expression engine.
 */
public record Lt(ExprNode left, ExprNode right) implements ExprNode {

    public Lt {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Lt of(ExprNode left, ExprNode right) {
        return new Lt(left, right);
    }
}
