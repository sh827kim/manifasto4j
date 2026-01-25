package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Len - 컬렉션의 길이 반환
 * 예: len(get("data.todos"))
 */
public record Len(ExprNode arg) implements ExprNode {

    public Len {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Len of(ExprNode arg) {
        return new Len(arg);
    }
}
