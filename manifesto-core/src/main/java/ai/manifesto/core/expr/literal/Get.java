package ai.manifesto.core.expr.literal;

import ai.manifesto.core.expr.ExprNode;

import java.util.Objects;

/**
 * Get - Snapshot의 특정 경로에서 값을 가져오는 표현식
 *
 * 경로 형식:
 * - data.count - data 필드의 count 값
 * - data.todos.0.title - data.todos 배열의 첫 번째 요소의 title
 * - input.title - input 필드의 title
 * - computed.total - computed 필드의 total
 * - system.status - system 필드의 status
 *
 * 특수 경로:
 * - $item - 컬렉션 필터링 중 현재 항목
 * - $index - 컬렉션 필터링 중 현재 인덱스
 * - $array - 컬렉션 필터링 중 전체 배열
 * - $system.uuid - 결정론적 UUID 생성
 * - $system.timestamp - 현재 타임스탬프
 *
 * 예: get("data.count"), get("$item.completed"), get("$system.uuid")
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
