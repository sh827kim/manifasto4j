package ai.manifesto.core.expr.comparison;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Eq - 두 값의 동등성 비교
 *
 * 예: eq(get("data.count"), lit(0))
 *     eq(get("input.title"), lit(""))
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
