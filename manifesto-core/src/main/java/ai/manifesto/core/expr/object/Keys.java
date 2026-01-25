package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Keys - 객체의 키들을 배열로 반환
 * 예: keys(get("data.user"))
 */
public record Keys(ExprNode obj) implements ExprNode {

    public Keys {
        Objects.requireNonNull(obj, "obj is required");
    }

    public static Keys of(ExprNode obj) {
        return new Keys(obj);
    }
}
