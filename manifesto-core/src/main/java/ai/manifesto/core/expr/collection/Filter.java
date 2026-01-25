package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Filter - 배열을 술어로 필터링
 * 술어는 $item, $index, $array 컨텍스트에서 평가됨
 *
 * 예: filter(get("data.todos"), get("$item.completed"))
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
