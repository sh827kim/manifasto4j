package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Gt는 Core 표현식 엔진에서 전달되는 gt 데이터를 담는 불변 레코드입니다.
 * EN: Gt is an immutable record carrying gt data in the Core expression engine.
 */
public record Gt(ExprNode left, ExprNode right) implements ExprNode {

    public Gt {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Gt of(ExprNode left, ExprNode right) {
        return new Gt(left, right);
    }
}
