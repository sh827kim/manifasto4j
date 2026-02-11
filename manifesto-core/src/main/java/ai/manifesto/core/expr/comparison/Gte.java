package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Gte는 Core 표현식 엔진에서 전달되는 gte 데이터를 담는 불변 레코드입니다.
 * EN: Gte is an immutable record carrying gte data in the Core expression engine.
 */
public record Gte(ExprNode left, ExprNode right) implements ExprNode {

    public Gte {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Gte of(ExprNode left, ExprNode right) {
        return new Gte(left, right);
    }
}
