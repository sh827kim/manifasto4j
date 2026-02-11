package ai.manifesto.core.expr.literal;

import ai.manifesto.core.expr.ExprNode;

/**
 * KR: Lit는 Core 표현식 엔진에서 전달되는 lit 데이터를 담는 불변 레코드입니다.
 * EN: Lit is an immutable record carrying lit data in the Core expression engine.
 */
public record Lit(Object value) implements ExprNode {

    /**
     * 리터럴 값으로부터 Lit 생성
     */
    public static Lit of(Object value) {
        return new Lit(value);
    }
}
