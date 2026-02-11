package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Values는 Core 표현식 엔진에서 전달되는 values 데이터를 담는 불변 레코드입니다.
 * EN: Values is an immutable record carrying values data in the Core expression engine.
 */
public record Values(ExprNode obj) implements ExprNode {

    public Values {
        Objects.requireNonNull(obj, "obj is required");
    }

    public static Values of(ExprNode obj) {
        return new Values(obj);
    }
}
