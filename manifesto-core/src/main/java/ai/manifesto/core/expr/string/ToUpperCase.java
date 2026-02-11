package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: ToUpperCase는 Core 표현식 엔진에서 전달되는 to upper case 데이터를 담는 불변 레코드입니다.
 * EN: ToUpperCase is an immutable record carrying to upper case data in the Core expression engine.
 */
public record ToUpperCase(ExprNode str) implements ExprNode {

    public ToUpperCase {
        Objects.requireNonNull(str, "str is required");
    }

    public static ToUpperCase of(ExprNode str) {
        return new ToUpperCase(str);
    }
}
