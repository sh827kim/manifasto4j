package ai.manifesto.app;

/**
 * KR: memory 저장 단위를 나타내는 레코드 타입입니다.
 * EN: Record type representing one persisted memory entry.
 */
public record StoredMemoryRecord(String key, Object value, long timestamp) {
}
