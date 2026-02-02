package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * MaxArray - 배열 최대값
 * 예: maxArray(get("data.values"))
 */
public record MaxArray(ExprNode array) implements ExprNode {

    public MaxArray {
        Objects.requireNonNull(array, "array is required");
    }

    public static MaxArray of(ExprNode array) {
        return new MaxArray(array);
    }
}
