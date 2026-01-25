package ai.manifesto.core.expr.type;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * Coalesce - null이 아닌 첫 값 반환
 * 모든 값이 null이면 null 반환
 *
 * 예: coalesce(get("data.nickname"), get("data.name"), lit("Anonymous"))
 */
public record Coalesce(List<ExprNode> args) implements ExprNode {

    public Coalesce {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Coalesce of(ExprNode... args) {
        return new Coalesce(List.of(args));
    }

    public static Coalesce of(List<ExprNode> args) {
        return new Coalesce(args);
    }
}
