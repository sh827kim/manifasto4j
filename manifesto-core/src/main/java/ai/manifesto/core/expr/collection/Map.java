package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Map - 배열을 변환 함수로 매핑
 * 매퍼는 $item, $index, $array 컨텍스트에서 평가됨
 *
 * 예: map(get("data.todos"), get("$item.title"))
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
