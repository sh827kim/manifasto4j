package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Abs는 Core 표현식 엔진에서 전달되는 abs 데이터를 담는 불변 레코드입니다.
 * EN: Abs is an immutable record carrying abs data in the Core expression engine.
 */
public record Abs(ExprNode arg) implements ExprNode {

    public Abs {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Abs of(ExprNode arg) {
        return new Abs(arg);
    }
}
