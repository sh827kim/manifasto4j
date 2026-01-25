package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Last - 배열의 마지막 요소 반환
 * 예: last(get("data.todos"))
 */
public record Last(ExprNode array) implements ExprNode {

    public Last {
        Objects.requireNonNull(array, "array is required");
    }

    public static Last of(ExprNode array) {
        return new Last(array);
    }
}
