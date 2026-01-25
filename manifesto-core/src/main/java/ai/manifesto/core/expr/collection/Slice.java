package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Slice - 배열의 부분 추출
 * 예: slice(get("data.todos"), lit(0), lit(5))
 */
public record Slice(ExprNode array, ExprNode start, ExprNode end) implements ExprNode {

    public Slice {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(start, "start is required");
        Objects.requireNonNull(end, "end is required");
    }

    public static Slice of(ExprNode array, ExprNode start, ExprNode end) {
        return new Slice(array, start, end);
    }
}
