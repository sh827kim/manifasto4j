package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * EndsWith - 접미사 일치 여부
 * 예: endsWith(get("data.name"), lit(".txt"))
 */
public record EndsWith(ExprNode str, ExprNode suffix) implements ExprNode {

    public EndsWith {
        Objects.requireNonNull(str, "str is required");
        Objects.requireNonNull(suffix, "suffix is required");
    }

    public static EndsWith of(ExprNode str, ExprNode suffix) {
        return new EndsWith(str, suffix);
    }
}
