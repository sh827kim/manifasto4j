package ai.manifesto.core.expr.conditional;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: If는 Core 표현식 엔진에서 전달되는 if 데이터를 담는 불변 레코드입니다.
 * EN: If is an immutable record carrying if data in the Core expression engine.
 */
public record If(ExprNode cond, ExprNode thenExpr, ExprNode elseExpr) implements ExprNode {

    public If {
        Objects.requireNonNull(cond, "cond is required");
        Objects.requireNonNull(thenExpr, "thenExpr is required");
        Objects.requireNonNull(elseExpr, "elseExpr is required");
    }

    public static If of(ExprNode cond, ExprNode thenExpr, ExprNode elseExpr) {
        return new If(cond, thenExpr, elseExpr);
    }
}
