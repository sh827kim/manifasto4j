package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Reduce - 배열을 누적 함수로 축약
 * 누적기는 $acc, $item, $index, $array 컨텍스트에서 평가됨
 *
 * 예: reduce(get("data.values"), add(get("$acc"), get("$item")), lit(0))
 */
public record Reduce(ExprNode array, ExprNode reducer, ExprNode initial) implements ExprNode {

    public Reduce {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(reducer, "reducer is required");
        Objects.requireNonNull(initial, "initial is required");
    }

    public static Reduce of(ExprNode array, ExprNode reducer, ExprNode initial) {
        return new Reduce(array, reducer, initial);
    }
}
