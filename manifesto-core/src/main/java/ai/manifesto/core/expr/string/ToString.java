package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * ToString - 문자열 변환
 * 예: toString(get("data.value"))
 */
public record ToString(ExprNode arg) implements ExprNode {

    public ToString {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static ToString of(ExprNode arg) {
        return new ToString(arg);
    }
}
