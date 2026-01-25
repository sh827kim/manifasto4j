package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Trim - 문자열의 앞뒤 공백 제거
 * 예: trim(get("data.text"))
 */
public record Trim(ExprNode str) implements ExprNode {

    public Trim {
        Objects.requireNonNull(str, "str is required");
    }

    public static Trim of(ExprNode str) {
        return new Trim(str);
    }
}
