package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Floor는 Core 표현식 엔진에서 전달되는 floor 데이터를 담는 불변 레코드입니다.
 * EN: Floor is an immutable record carrying floor data in the Core expression engine.
 */
public record Floor(ExprNode arg) implements ExprNode {

    public Floor {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Floor of(ExprNode arg) {
        return new Floor(arg);
    }
}
