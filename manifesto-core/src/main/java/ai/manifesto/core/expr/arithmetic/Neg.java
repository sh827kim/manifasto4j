package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Neg는 Core 표현식 엔진에서 전달되는 neg 데이터를 담는 불변 레코드입니다.
 * EN: Neg is an immutable record carrying neg data in the Core expression engine.
 */
public record Neg(ExprNode arg) implements ExprNode {

    public Neg {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Neg of(ExprNode arg) {
        return new Neg(arg);
    }
}
