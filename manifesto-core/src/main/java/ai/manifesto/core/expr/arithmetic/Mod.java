package ai.manifesto.core.expr.arithmetic;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Mod는 Core 표현식 엔진에서 전달되는 mod 데이터를 담는 불변 레코드입니다.
 * EN: Mod is an immutable record carrying mod data in the Core expression engine.
 */
public record Mod(ExprNode left, ExprNode right) implements ExprNode {

    public Mod {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(right, "right is required");
    }

    public static Mod of(ExprNode left, ExprNode right) {
        return new Mod(left, right);
    }
}
