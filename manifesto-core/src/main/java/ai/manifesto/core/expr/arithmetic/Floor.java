package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Floor - 바닥 함수
 * 예: floor(get("data.value"))
 */
public record Floor(ExprNode arg) implements ExprNode {

    public Floor {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Floor of(ExprNode arg) {
        return new Floor(arg);
    }
}
