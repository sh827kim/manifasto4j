package ai.manifesto.core.expr.literal;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * KR: Get는 Core 표현식 엔진에서 전달되는 get 데이터를 담는 불변 레코드입니다.
 * EN: Get is an immutable record carrying get data in the Core expression engine.
 */
public record Get(String path) implements ExprNode {

    public Get {
        Objects.requireNonNull(path, "path is required");
    }

    /**
     * 경로로부터 Get 생성
     */
    public static Get of(String path) {
        return new Get(path);
    }
}
