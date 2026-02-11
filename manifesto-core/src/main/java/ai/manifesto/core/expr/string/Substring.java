package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Substring는 Core 표현식 엔진에서 전달되는 substring 데이터를 담는 불변 레코드입니다.
 * EN: Substring is an immutable record carrying substring data in the Core expression engine.
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
