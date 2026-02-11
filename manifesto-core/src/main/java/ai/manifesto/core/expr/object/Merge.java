package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: Merge는 Core 표현식 엔진에서 전달되는 merge 데이터를 담는 불변 레코드입니다.
 * EN: Merge is an immutable record carrying merge data in the Core expression engine.
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
