package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Eq는 Core 표현식 엔진에서 전달되는 eq 데이터를 담는 불변 레코드입니다.
 * EN: Eq is an immutable record carrying eq data in the Core expression engine.
 */
public record Eq(ExprNode left, ExprNode right) implements ExprNode {

    public Eq {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Eq of(ExprNode left, ExprNode right) {
        return new Eq(left, right);
    }
}
