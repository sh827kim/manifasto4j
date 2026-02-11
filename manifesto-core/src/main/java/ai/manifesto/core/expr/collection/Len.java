package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Len는 Core 표현식 엔진에서 전달되는 len 데이터를 담는 불변 레코드입니다.
 * EN: Len is an immutable record carrying len data in the Core expression engine.
 */
public record Len(ExprNode arg) implements ExprNode {

    public Len {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Len of(ExprNode arg) {
        return new Len(arg);
    }
}
