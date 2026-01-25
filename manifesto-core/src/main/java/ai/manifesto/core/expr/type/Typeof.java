package ai.manifesto.core.expr.type;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Typeof - 값의 타입 반환
 * 반환값: "null", "boolean", "number", "string", "array", "object"
 *
 * 예: typeof(get("data.value"))
 */
public record Typeof(ExprNode arg) implements ExprNode {

    public Typeof {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Typeof of(ExprNode arg) {
        return new Typeof(arg);
    }
}
