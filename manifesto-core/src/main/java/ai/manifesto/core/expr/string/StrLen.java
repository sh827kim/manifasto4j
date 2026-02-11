package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: StrLen는 Core 표현식 엔진에서 전달되는 str len 데이터를 담는 불변 레코드입니다.
 * EN: StrLen is an immutable record carrying str len data in the Core expression engine.
 */
public record StrLen(ExprNode str) implements ExprNode {

    public StrLen {
        Objects.requireNonNull(str, "str is required");
    }

    public static StrLen of(ExprNode str) {
        return new StrLen(str);
    }
}
