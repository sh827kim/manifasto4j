package ai.manifesto.core.expr.collection;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Slice는 Core 표현식 엔진에서 전달되는 slice 데이터를 담는 불변 레코드입니다.
 * EN: Slice is an immutable record carrying slice data in the Core expression engine.
 */
public record Slice(ExprNode array, ExprNode start, ExprNode end) implements ExprNode {

    public Slice {
        Objects.requireNonNull(array, "array is required");
        Objects.requireNonNull(start, "start is required");
    }

    public static Slice of(ExprNode array, ExprNode start, ExprNode end) {
        return new Slice(array, start, end);
    }
}
