package ai.manifesto.core.expr.logical;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: Or는 Core 표현식 엔진에서 전달되는 or 데이터를 담는 불변 레코드입니다.
 * EN: Or is an immutable record carrying or data in the Core expression engine.
 */
public record Or(List<ExprNode> args) implements ExprNode {

    public Or {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Or of(ExprNode... args) {
        return new Or(List.of(args));
    }

    public static Or of(List<ExprNode> args) {
        return new Or(args);
    }
}
