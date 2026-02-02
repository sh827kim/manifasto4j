package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * StrLen - 문자열 길이
 * 예: strLen(get("data.title"))
 */
public record StrLen(ExprNode str) implements ExprNode {

    public StrLen {
        Objects.requireNonNull(str, "str is required");
    }

    public static StrLen of(ExprNode str) {
        return new StrLen(str);
    }
}
