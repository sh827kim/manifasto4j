package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: MaxArray는 Core 표현식 엔진에서 전달되는 max array 데이터를 담는 불변 레코드입니다.
 * EN: MaxArray is an immutable record carrying max array data in the Core expression engine.
 */
public record MaxArray(ExprNode array) implements ExprNode {

    public MaxArray {
        Objects.requireNonNull(array, "array is required");
    }

    public static MaxArray of(ExprNode array) {
        return new MaxArray(array);
    }
}
