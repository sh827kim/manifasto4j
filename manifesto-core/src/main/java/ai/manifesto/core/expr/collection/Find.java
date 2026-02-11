package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Find는 Core 표현식 엔진에서 전달되는 find 데이터를 담는 불변 레코드입니다.
 * EN: Find is an immutable record carrying find data in the Core expression engine.
 */
public record Find(ExprNode array, ExprNode predicate) implements ExprNode {

    public Find {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(predicate, "predicate is required");
    }

    public static Find of(ExprNode array, ExprNode predicate) {
        return new Find(array, predicate);
    }
}
