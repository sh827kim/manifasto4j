package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Split - 문자열 분리
 * 예: split(get("data.tags"), lit(","))
 */
public record Split(ExprNode str, ExprNode delimiter) implements ExprNode {

    public Split {
        Objects.requireNonNull(str, "str is required");
        Objects.requireNonNull(delimiter, "delimiter is required");
    }

    public static Split of(ExprNode str, ExprNode delimiter) {
        return new Split(str, delimiter);
    }
}
