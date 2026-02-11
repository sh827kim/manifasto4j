package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Neq는 Core 표현식 엔진에서 전달되는 neq 데이터를 담는 불변 레코드입니다.
 * EN: Neq is an immutable record carrying neq data in the Core expression engine.
 */
public record Neq(ExprNode left, ExprNode right) implements ExprNode {

    public Neq {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Neq of(ExprNode left, ExprNode right) {
        return new Neq(left, right);
    }
}
