package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Lte는 Core 표현식 엔진에서 전달되는 lte 데이터를 담는 불변 레코드입니다.
 * EN: Lte is an immutable record carrying lte data in the Core expression engine.
 */
public record Lte(ExprNode left, ExprNode right) implements ExprNode {

    public Lte {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Lte of(ExprNode left, ExprNode right) {
        return new Lte(left, right);
    }
}
