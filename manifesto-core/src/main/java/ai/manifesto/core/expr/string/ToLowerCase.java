package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * ToLowerCase - 문자열 소문자 변환
 * 예: toLowerCase(get("data.text"))
 */
public record ToLowerCase(ExprNode str) implements ExprNode {

    public ToLowerCase {
        Objects.requireNonNull(str, "str is required");
    }

    public static ToLowerCase of(ExprNode str) {
        return new ToLowerCase(str);
    }
}
