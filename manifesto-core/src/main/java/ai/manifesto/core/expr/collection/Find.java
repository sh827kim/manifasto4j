package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Find - 술어를 만족하는 첫 요소 반환
 * 술어는 $item, $index, $array 컨텍스트에서 평가됨
 *
 * 예: find(get("data.todos"), eq(get("$item.id"), get("input.todoId")))
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
