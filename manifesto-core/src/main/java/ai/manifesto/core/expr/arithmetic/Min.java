package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * Min - 최소값
 * 예: min(get("data.a"), get("data.b"), lit(3))
 */
public record Min(List<ExprNode> args) implements ExprNode {

    public Min {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Min of(ExprNode... args) {
        return new Min(List.of(args));
    }

    public static Min of(List<ExprNode> args) {
        return new Min(args);
    }
}
