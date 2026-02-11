package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Map;
import java.util.Objects;

/**
 * KR: ObjectExpr는 Core 표현식 엔진에서 전달되는 object expr 데이터를 담는 불변 레코드입니다.
 * EN: ObjectExpr is an immutable record carrying object expr data in the Core expression engine.
 */
public record ObjectExpr(Map<String, ExprNode> fields) implements ExprNode {

    public ObjectExpr {
        Objects.requireNonNull(fields, "fields is required");
        fields = Map.copyOf(fields);
    }

    public static ObjectExpr of(Map<String, ExprNode> fields) {
        return new ObjectExpr(fields);
    }
}
