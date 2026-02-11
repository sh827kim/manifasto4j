package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Round는 Core 표현식 엔진에서 전달되는 round 데이터를 담는 불변 레코드입니다.
 * EN: Round is an immutable record carrying round data in the Core expression engine.
 */
public record Round(ExprNode arg) implements ExprNode {

    public Round {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Round of(ExprNode arg) {
        return new Round(arg);
    }
}
