package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: Max는 Core 표현식 엔진에서 전달되는 max 데이터를 담는 불변 레코드입니다.
 * EN: Max is an immutable record carrying max data in the Core expression engine.
 */
public record Max(List<ExprNode> args) implements ExprNode {

    public Max {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Max of(ExprNode... args) {
        return new Max(List.of(args));
    }

    public static Max of(List<ExprNode> args) {
        return new Max(args);
    }
}
