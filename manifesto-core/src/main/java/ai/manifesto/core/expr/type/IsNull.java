package ai.manifesto.core.expr.type;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * IsNull - 값이 null인지 확인
 * 예: isNull(get("data.description"))
 */
public record IsNull(ExprNode arg) implements ExprNode {

    public IsNull {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static IsNull of(ExprNode arg) {
        return new IsNull(arg);
    }
}
