package ai.manifesto.world.persistence;

/**
 * KR: world edge 조회의 filter/limit 계약입니다.
 * EN: Filter/limit contract for world edge queries.
 */
public record EdgeQuery(
    String fromWorldId,
    String toWorldId,
    String proposalId,
    String decisionId,
    int limit
) {
    public static EdgeQuery defaults() {
        return new EdgeQuery(null, null, null, null, 0);
    }

    public int safeLimit() {
        return Math.max(0, limit);
    }
}
