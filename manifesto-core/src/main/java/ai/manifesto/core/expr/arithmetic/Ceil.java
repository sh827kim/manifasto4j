package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Ceil는 Core 표현식 엔진에서 전달되는 ceil 데이터를 담는 불변 레코드입니다.
 * EN: Ceil is an immutable record carrying ceil data in the Core expression engine.
 */
public record Ceil(ExprNode arg) implements ExprNode {

    public Ceil {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Ceil of(ExprNode arg) {
        return new Ceil(arg);
    }
}
