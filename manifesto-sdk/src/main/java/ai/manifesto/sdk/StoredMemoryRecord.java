package ai.manifesto.sdk;

/**
 * KR: SDK 메모리 저장 레코드입니다.
 * EN: SDK memory storage record.
 */
public record StoredMemoryRecord(
    String key,
    Object value,
    long timestamp
) {
}
