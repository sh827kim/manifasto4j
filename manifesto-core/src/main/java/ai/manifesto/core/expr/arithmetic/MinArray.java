package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * MinArray - 배열 최소값
 * 예: minArray(get("data.values"))
 */
public record MinArray(ExprNode array) implements ExprNode {

    public MinArray {
        Objects.requireNonNull(array, "array is required");
    }

    public static MinArray of(ExprNode array) {
        return new MinArray(array);
    }
}
