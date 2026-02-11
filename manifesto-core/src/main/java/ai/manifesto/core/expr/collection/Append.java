package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: Append는 Core 표현식 엔진에서 전달되는 append 데이터를 담는 불변 레코드입니다.
 * EN: Append is an immutable record carrying append data in the Core expression engine.
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
