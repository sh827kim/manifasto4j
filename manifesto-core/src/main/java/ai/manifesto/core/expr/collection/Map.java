package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Map는 Core 표현식 엔진에서 전달되는 map 데이터를 담는 불변 레코드입니다.
 * EN: Map is an immutable record carrying map data in the Core expression engine.
 */
public record Map(ExprNode array, ExprNode mapper) implements ExprNode {

    public Map {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(mapper, "mapper is required");
    }

    public static Map of(ExprNode array, ExprNode mapper) {
        return new Map(array, mapper);
    }
}
