package ai.manifesto.sdk;

/**
 * KR: SDK memory 조회 요청입니다.
 * EN: SDK memory recall request.
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
