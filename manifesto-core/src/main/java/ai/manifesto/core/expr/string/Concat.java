package ai.manifesto.core.expr.string;

import ai.manifesto.core.expr.ExprNode;

import java.util.List;
import java.util.Objects;

/**
 * KR: Concat는 Core 표현식 엔진에서 전달되는 concat 데이터를 담는 불변 레코드입니다.
 * EN: Concat is an immutable record carrying concat data in the Core expression engine.
 */
public record Concat(List<ExprNode> args) implements ExprNode {

    public Concat {
        Objects.requireNonNull(args, "args is required");
        args = List.copyOf(args);
    }

    public static Concat of(ExprNode... args) {
        return new Concat(List.of(args));
    }

    public static Concat of(List<ExprNode> args) {
        return new Concat(args);
    }
}
