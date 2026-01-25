package ai.manifesto.core.expr.logical;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * Or - 논리합 (||)
 * 하나 이상의 인자가 참이면 전체 결과가 참
 * 짧은 회로 평가: 첫 참값을 만나면 즉시 반환
 *
 * 예: or(get("system.isAdmin"), get("system.isOwner"))
 */
public record Or(List<ExprNode> args) implements ExprNode {

    public Or {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Or of(ExprNode... args) {
        return new Or(List.of(args));
    }

    public static Or of(List<ExprNode> args) {
        return new Or(args);
    }
}
