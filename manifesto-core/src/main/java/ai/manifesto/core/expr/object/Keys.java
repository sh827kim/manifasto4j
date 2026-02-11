package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Keys는 Core 표현식 엔진에서 전달되는 keys 데이터를 담는 불변 레코드입니다.
 * EN: Keys is an immutable record carrying keys data in the Core expression engine.
 */
public record Keys(ExprNode obj) implements ExprNode {

    public Keys {
        Objects.requireNonNull(obj, "obj is required");
    }

    public static Keys of(ExprNode obj) {
        return new Keys(obj);
    }
}
