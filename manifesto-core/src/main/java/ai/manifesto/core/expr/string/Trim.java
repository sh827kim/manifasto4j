package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Trim는 Core 표현식 엔진에서 전달되는 trim 데이터를 담는 불변 레코드입니다.
 * EN: Trim is an immutable record carrying trim data in the Core expression engine.
 */
public record Trim(ExprNode str) implements ExprNode {

    public Trim {
        Objects.requireNonNull(str, "str is required");
    }

    public static Trim of(ExprNode str) {
        return new Trim(str);
    }
}
