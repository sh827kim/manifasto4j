package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Filter는 Core 표현식 엔진에서 전달되는 filter 데이터를 담는 불변 레코드입니다.
 * EN: Filter is an immutable record carrying filter data in the Core expression engine.
 */
public record Filter(ExprNode array, ExprNode predicate) implements ExprNode {

    public Filter {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(predicate, "predicate is required");
    }

    public static Filter of(ExprNode array, ExprNode predicate) {
        return new Filter(array, predicate);
    }
}
