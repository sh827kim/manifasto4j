package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Includes는 Core 표현식 엔진에서 전달되는 includes 데이터를 담는 불변 레코드입니다.
 * EN: Includes is an immutable record carrying includes data in the Core expression engine.
 */
public record Includes(ExprNode array, ExprNode item) implements ExprNode {

    public Includes {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(item, "item is required");
    }

    public static Includes of(ExprNode array, ExprNode item) {
        return new Includes(array, item);
    }
}
