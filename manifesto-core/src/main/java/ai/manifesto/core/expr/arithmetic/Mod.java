package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Mod - 나머지 (%)
 * 예: mod(get("data.count"), lit(2))
 */
public record Mod(ExprNode left, ExprNode right) implements ExprNode {

    public Mod {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Mod of(ExprNode left, ExprNode right) {
        return new Mod(left, right);
    }
}
