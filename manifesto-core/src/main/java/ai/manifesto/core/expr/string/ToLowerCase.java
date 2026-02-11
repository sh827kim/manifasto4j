package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: ToLowerCase는 Core 표현식 엔진에서 전달되는 to lower case 데이터를 담는 불변 레코드입니다.
 * EN: ToLowerCase is an immutable record carrying to lower case data in the Core expression engine.
 */
public record ToLowerCase(ExprNode str) implements ExprNode {

    public ToLowerCase {
        Objects.requireNonNull(str, "str is required");
    }

    public static ToLowerCase of(ExprNode str) {
        return new ToLowerCase(str);
    }
}
