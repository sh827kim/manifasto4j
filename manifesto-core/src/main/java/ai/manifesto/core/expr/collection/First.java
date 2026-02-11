package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: First는 Core 표현식 엔진에서 전달되는 first 데이터를 담는 불변 레코드입니다.
 * EN: First is an immutable record carrying first data in the Core expression engine.
 */
public record First(ExprNode array) implements ExprNode {

    public First {
        Objects.requireNonNull(array, "array is required");
    }

    public static First of(ExprNode array) {
        return new First(array);
    }
}
