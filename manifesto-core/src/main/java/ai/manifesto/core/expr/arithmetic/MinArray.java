package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: MinArray는 Core 표현식 엔진에서 전달되는 min array 데이터를 담는 불변 레코드입니다.
 * EN: MinArray is an immutable record carrying min array data in the Core expression engine.
 */
public record MinArray(ExprNode array) implements ExprNode {

    public MinArray {
        Objects.requireNonNull(array, "array is required");
    }

    public static MinArray of(ExprNode array) {
        return new MinArray(array);
    }
}
