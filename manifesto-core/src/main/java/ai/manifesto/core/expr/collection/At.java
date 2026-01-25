package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * At - 배열의 특정 인덱스 요소 반환
 * 예: at(get("data.todos"), lit(0))
 */
public record At(ExprNode array, ExprNode index) implements ExprNode {

    public At {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(index, "index is required");
    }

    public static At of(ExprNode array, ExprNode index) {
        return new At(array, index);
    }
}
