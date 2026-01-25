package ai.manifesto.core.expr.logical;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Not - 논리 부정 (!)
 * 참을 거짓으로, 거짓을 참으로 변환
 *
 * 예: not(eq(get("data.status"), lit("done")))
 */
public record Not(ExprNode arg) implements ExprNode {

    public Not {
        Objects.requireNonNull(arg, "arg is required");
    }

    public static Not of(ExprNode arg) {
        return new Not(arg);
    }
}
