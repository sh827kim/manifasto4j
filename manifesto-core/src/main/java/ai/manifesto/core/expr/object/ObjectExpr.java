package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Map;
import java.util.Objects;

/**
 * ObjectExpr - 객체 리터럴 생성
 * 각 필드의 값은 표현식으로 평가됨
 *
 * 예: object({"id": get("$system.uuid"), "title": get("input.title")})
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
