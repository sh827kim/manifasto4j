package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * Concat - 문자열 연결
 * 모든 인자를 문자열로 변환 후 연결
 *
 * 예: concat(lit("Hello"), lit(" "), get("input.name"))
 */
public record Concat(List<ExprNode> args) implements ExprNode {

    public Concat {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Concat of(ExprNode... args) {
        return new Concat(List.of(args));
    }

    public static Concat of(List<ExprNode> args) {
        return new Concat(args);
    }
}
