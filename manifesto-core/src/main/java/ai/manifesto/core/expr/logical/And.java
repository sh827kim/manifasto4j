package ai.manifesto.core.expr.logical;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * And - 논리곱 (&&)
 * 모든 인자가 참이어야 전체 결과가 참
 * 짧은 회로 평가: 첫 거짓값을 만나면 즉시 반환
 *
 * 예: and(eq(get("input.title"), lit("")), get("system.isAdmin"))
 */
public record And(List<ExprNode> args) implements ExprNode {

    public And {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static And of(ExprNode... args) {
        return new And(List.of(args));
    }

    public static And of(List<ExprNode> args) {
        return new And(args);
    }
}
