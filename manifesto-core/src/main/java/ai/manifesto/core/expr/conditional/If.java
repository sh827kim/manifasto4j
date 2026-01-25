package ai.manifesto.core.expr.conditional;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * If - 조건부 표현식
 * cond가 참이면 thenExpr, 거짓이면 elseExpr 평가
 *
 * 예: if(eq(get("data.status"), lit("pending")), lit("대기중"), lit("완료"))
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
