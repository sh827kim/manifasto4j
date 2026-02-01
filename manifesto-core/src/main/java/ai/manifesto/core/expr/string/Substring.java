package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Substring - 문자열의 부분 추출
 * 예: substring(get("data.text"), lit(0), lit(5))
 */
public record Substring(ExprNode str, ExprNode start, ExprNode end) implements ExprNode {

    public Substring {
        Objects.requireNonNull(str, "str is required");
        Objects.requireNonNull(start, "start is required");
    }

    public static Substring of(ExprNode str, ExprNode start, ExprNode end) {
        return new Substring(str, start, end);
    }
}
