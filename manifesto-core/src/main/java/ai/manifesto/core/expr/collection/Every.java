package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Every는 Core 표현식 엔진에서 전달되는 every 데이터를 담는 불변 레코드입니다.
 * EN: Every is an immutable record carrying every data in the Core expression engine.
 */
public record Every(ExprNode array, ExprNode predicate) implements ExprNode {

    public Every {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(predicate, "predicate is required");
    }

    public static Every of(ExprNode array, ExprNode predicate) {
        return new Every(array, predicate);
    }
}
