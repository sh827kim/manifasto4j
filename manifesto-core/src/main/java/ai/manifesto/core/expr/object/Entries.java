package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Entries - 객체의 [key, value] 쌍을 배열로 반환
 * 예: entries(get("data.options"))
 */
public record Entries(ExprNode obj) implements ExprNode {

    public Entries {
        Objects.requireNonNull(obj, "obj is required");
    }

    public static Entries of(ExprNode obj) {
        return new Entries(obj);
    }
}
