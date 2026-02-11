package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: Min는 Core 표현식 엔진에서 전달되는 min 데이터를 담는 불변 레코드입니다.
 * EN: Min is an immutable record carrying min data in the Core expression engine.
 */
public record Min(List<ExprNode> args) implements ExprNode {

    public Min {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Min of(ExprNode... args) {
        return new Min(List.of(args));
    }

    public static Min of(List<ExprNode> args) {
        return new Min(args);
    }
}
