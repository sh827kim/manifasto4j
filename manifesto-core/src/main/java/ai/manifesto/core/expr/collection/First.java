package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * First - 배열의 첫 요소 반환
 * 예: first(get("data.todos"))
 */
public record First(ExprNode array) implements ExprNode {

    public First {
        Objects.requireNonNull(array, "array is required");
    }

    public static First of(ExprNode array) {
        return new First(array);
    }
}
