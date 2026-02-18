package ai.manifesto.runtime;

import java.util.List;

/**
 * KR: memory 조회 결과입니다.
 * EN: Memory recall result.
 */
public record RecallResult(
    List<StoredMemoryRecord> records,
    boolean contextFrozen,
    String contextToken,
    String failureMarker
) {
    public RecallResult(List<StoredMemoryRecord> records) {
        this(records, false, null, null);
    }
}
