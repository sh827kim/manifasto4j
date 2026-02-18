package ai.manifesto.runtime;

/**
 * KR: memory 조회 요청 조건입니다.
 * EN: Memory recall request criteria.
 */
public record RecallRequest(
    String keyPrefix,
    int limit,
    boolean freezeContext,
    String contextToken
) {
    public RecallRequest(String keyPrefix, int limit) {
        this(keyPrefix, limit, false, null);
    }

    public RecallRequest {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }
    }
}
