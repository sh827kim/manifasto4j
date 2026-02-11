package ai.manifesto.core.expr.logical;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Not는 Core 표현식 엔진에서 전달되는 not 데이터를 담는 불변 레코드입니다.
 * EN: Not is an immutable record carrying not data in the Core expression engine.
 */
public record Not(ExprNode arg) implements ExprNode {

    public Not {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Not of(ExprNode arg) {
        return new Not(arg);
    }
}
