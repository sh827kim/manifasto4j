package ai.manifesto.core.expr.object;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Entries는 Core 표현식 엔진에서 전달되는 entries 데이터를 담는 불변 레코드입니다.
 * EN: Entries is an immutable record carrying entries data in the Core expression engine.
 */
public record Entries(ExprNode obj) implements ExprNode {

    public Entries {
        Objects.requireNonNull(obj, "obj is required");
    }

    public static Entries of(ExprNode obj) {
        return new Entries(obj);
    }
}
