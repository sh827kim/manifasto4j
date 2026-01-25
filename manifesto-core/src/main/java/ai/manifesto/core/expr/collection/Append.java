package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * Append - 배열에 항목들을 추가
 * 새로운 배열을 반환 (원본은 수정되지 않음)
 *
 * 예: append(get("data.todos"), lit({id: "123", title: "New"}))
 */
public record Append(ExprNode array, List<ExprNode> items) implements ExprNode {

    public Append {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(items, "items is required");
        items = List.copyOf(items);
    }

    public static Append of(ExprNode array, ExprNode... items) {
        return new Append(array, List.of(items));
    }

    public static Append of(ExprNode array, List<ExprNode> items) {
        return new Append(array, items);
    }
}
