package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Pow - 거듭제곱
 * 예: pow(get("data.base"), lit(2))
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
