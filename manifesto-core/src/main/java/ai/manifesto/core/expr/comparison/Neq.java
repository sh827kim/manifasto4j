package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Neq - 두 값의 부동등성 비교
 * 예: neq(get("data.status"), lit("done"))
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
