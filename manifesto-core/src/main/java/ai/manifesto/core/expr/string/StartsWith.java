package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * StartsWith - 접두사 일치 여부
 * 예: startsWith(get("data.name"), lit("user_"))
 */
public record StartsWith(ExprNode str, ExprNode prefix) implements ExprNode {

    public StartsWith {
        Objects.requireNonNull(str, "str is required");
        Objects.requireNonNull(prefix, "prefix is required");
    }

    public static StartsWith of(ExprNode str, ExprNode prefix) {
        return new StartsWith(str, prefix);
    }
}
