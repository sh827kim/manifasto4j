package ai.manifesto.core.expr.type;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: IsNull는 Core 표현식 엔진에서 전달되는 is null 데이터를 담는 불변 레코드입니다.
 * EN: IsNull is an immutable record carrying is null data in the Core expression engine.
 */
public record IsNull(ExprNode arg) implements ExprNode {

    public IsNull {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static IsNull of(ExprNode arg) {
        return new IsNull(arg);
    }
}
