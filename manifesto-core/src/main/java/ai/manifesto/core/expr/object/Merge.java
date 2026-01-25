package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * Merge - 여러 객체를 얕은 병합
 * 뒤쪽 객체의 속성이 앞쪽을 덮어씀
 *
 * 예: merge(get("data.defaults"), get("input.overrides"))
 */
public record Merge(List<ExprNode> objects) implements ExprNode {

    public Merge {
        Objects.requireNonNull(objects, "objects is required");
        objects = List.copyOf(objects);
    }

    public static Merge of(ExprNode... objects) {
        return new Merge(List.of(objects));
    }

    public static Merge of(List<ExprNode> objects) {
        return new Merge(objects);
    }
}
