package ai.manifesto.core.expr.logical;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: And는 Core 표현식 엔진에서 전달되는 and 데이터를 담는 불변 레코드입니다.
 * EN: And is an immutable record carrying and data in the Core expression engine.
 */
public record And(List<ExprNode> args) implements ExprNode {

    public And {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static And of(ExprNode... args) {
        return new And(List.of(args));
    }

    public static And of(List<ExprNode> args) {
        return new And(args);
    }
}
