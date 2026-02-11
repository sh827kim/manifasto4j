package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: At는 Core 표현식 엔진에서 전달되는 at 데이터를 담는 불변 레코드입니다.
 * EN: At is an immutable record carrying at data in the Core expression engine.
 */
public record At(ExprNode array, ExprNode index) implements ExprNode {

    public At {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(index, "index is required");
    }

    public static At of(ExprNode array, ExprNode index) {
        return new At(array, index);
    }
}
