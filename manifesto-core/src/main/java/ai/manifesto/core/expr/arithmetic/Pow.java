package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Pow는 Core 표현식 엔진에서 전달되는 pow 데이터를 담는 불변 레코드입니다.
 * EN: Pow is an immutable record carrying pow data in the Core expression engine.
 */
public record Pow(ExprNode base, ExprNode exponent) implements ExprNode {

    public Pow {
        Objects.requireNonNull(base, "base is required");
        Objects.requireNonNull(exponent, "exponent is required");
    }

    public static Pow of(ExprNode base, ExprNode exponent) {
        return new Pow(base, exponent);
    }
}
