package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * SumArray - 배열 합계
 * 예: sumArray(get("data.values"))
 */
public record SumArray(ExprNode array) implements ExprNode {

    public SumArray {
        Objects.requireNonNull(array, "array is required");
    }

    public static SumArray of(ExprNode array) {
        return new SumArray(array);
    }
}
