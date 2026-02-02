package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * ToUpperCase - 문자열 대문자 변환
 * 예: toUpperCase(get("data.title"))
 */
public record ToUpperCase(ExprNode str) implements ExprNode {

    public ToUpperCase {
        Objects.requireNonNull(str, "str is required");
    }

    public static ToUpperCase of(ExprNode str) {
        return new ToUpperCase(str);
    }
}
