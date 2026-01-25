package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Values - 객체의 값들을 배열로 반환
 * 예: values(get("data.metadata"))
 */
public record Values(ExprNode obj) implements ExprNode {

    public Values {
        Objects.requireNonNull(obj, "obj is required");
    }

    public static Values of(ExprNode obj) {
        return new Values(obj);
    }
}
