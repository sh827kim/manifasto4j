package ai.manifesto.core.expr.literal;

import ai.manifesto.core.expr.ExprNode;

/**
 * Lit - 리터럴 값을 나타내는 표현식
 *
 * 예: lit(42), lit("hello"), lit(true), lit(null)
 */
public record Lit(Object value) implements ExprNode {

    /**
     * 리터럴 값으로부터 Lit 생성
     */
    public static Lit of(Object value) {
        return new Lit(value);
    }
}
