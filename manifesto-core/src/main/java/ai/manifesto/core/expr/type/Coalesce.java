package ai.manifesto.core.expr.type;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: Coalesce는 Core 표현식 엔진에서 전달되는 coalesce 데이터를 담는 불변 레코드입니다.
 * EN: Coalesce is an immutable record carrying coalesce data in the Core expression engine.
 */
public record Coalesce(List<ExprNode> args) implements ExprNode {

    public Coalesce {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Coalesce of(ExprNode... args) {
        return new Coalesce(List.of(args));
    }

    public static Coalesce of(List<ExprNode> args) {
        return new Coalesce(args);
    }
}
