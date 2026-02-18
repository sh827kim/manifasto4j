package ai.manifesto.sdk;

import java.util.List;

/**
 * KR: SDK memory 조회 결과입니다.
 * EN: SDK memory recall result.
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
