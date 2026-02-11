package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: ToString는 Core 표현식 엔진에서 전달되는 to string 데이터를 담는 불변 레코드입니다.
 * EN: ToString is an immutable record carrying to string data in the Core expression engine.
 */
public record ToString(ExprNode arg) implements ExprNode {

    public ToString {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static ToString of(ExprNode arg) {
        return new ToString(arg);
    }
}
