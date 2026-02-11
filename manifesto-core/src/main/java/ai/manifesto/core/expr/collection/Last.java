package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Last는 Core 표현식 엔진에서 전달되는 last 데이터를 담는 불변 레코드입니다.
 * EN: Last is an immutable record carrying last data in the Core expression engine.
 */
public record Last(ExprNode array) implements ExprNode {

    public Last {
        Objects.requireNonNull(array, "array is required");
    }

    public static Last of(ExprNode array) {
        return new Last(array);
    }
}
