package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * Max - 최대값
 * 예: max(get("data.a"), get("data.b"), lit(3))
 */
public record Max(List<ExprNode> args) implements ExprNode {

    public Max {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Max of(ExprNode... args) {
        return new Max(List.of(args));
    }

    public static Max of(List<ExprNode> args) {
        return new Max(args);
    }
}
