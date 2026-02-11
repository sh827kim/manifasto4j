package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: SumArray는 Core 표현식 엔진에서 전달되는 sum array 데이터를 담는 불변 레코드입니다.
 * EN: SumArray is an immutable record carrying sum array data in the Core expression engine.
 */
public record SumArray(ExprNode array) implements ExprNode {

    public SumArray {
        Objects.requireNonNull(array, "array is required");
    }

    public static SumArray of(ExprNode array) {
        return new SumArray(array);
    }
}
