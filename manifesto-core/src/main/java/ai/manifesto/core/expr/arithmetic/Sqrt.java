package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Sqrt는 Core 표현식 엔진에서 전달되는 sqrt 데이터를 담는 불변 레코드입니다.
 * EN: Sqrt is an immutable record carrying sqrt data in the Core expression engine.
 */
public record Sqrt(ExprNode arg) implements ExprNode {

    public Sqrt {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Sqrt of(ExprNode arg) {
        return new Sqrt(arg);
    }
}
