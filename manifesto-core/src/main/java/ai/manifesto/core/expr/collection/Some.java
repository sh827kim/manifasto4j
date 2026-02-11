package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Some는 Core 표현식 엔진에서 전달되는 some 데이터를 담는 불변 레코드입니다.
 * EN: Some is an immutable record carrying some data in the Core expression engine.
 */
public record Some(ExprNode array, ExprNode predicate) implements ExprNode {

    public Some {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(predicate, "predicate is required");
    }

    public static Some of(ExprNode array, ExprNode predicate) {
        return new Some(array, predicate);
    }
}
