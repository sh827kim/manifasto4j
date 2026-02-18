package ai.manifesto.app;

import java.util.List;

/**
 * KR: memory 조회 결과입니다.
 * EN: Memory recall result.
 */
public record RecallResult(List<StoredMemoryRecord> records) {
}
