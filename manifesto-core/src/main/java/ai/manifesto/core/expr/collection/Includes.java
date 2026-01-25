package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Includes - 배열이 특정 항목을 포함하는지 확인
 * 예: includes(get("data.tags"), lit("urgent"))
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
