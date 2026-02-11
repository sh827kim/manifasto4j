package ai.manifesto.core.expr.type;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Typeof는 Core 표현식 엔진에서 전달되는 typeof 데이터를 담는 불변 레코드입니다.
 * EN: Typeof is an immutable record carrying typeof data in the Core expression engine.
 */
public record Typeof(ExprNode arg) implements ExprNode {

    public Typeof {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Typeof of(ExprNode arg) {
        return new Typeof(arg);
    }
}
