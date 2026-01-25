package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Some - 하나 이상의 요소가 술어를 만족하는지 확인
 * 술어는 $item, $index, $array 컨텍스트에서 평가됨
 *
 * 예: some(get("data.todos"), get("$item.completed"))
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
