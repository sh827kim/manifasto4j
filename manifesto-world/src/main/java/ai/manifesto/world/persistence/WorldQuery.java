package ai.manifesto.world.persistence;

/**
 * KR: world 조회의 filter/sort/limit 계약입니다.
 * EN: Filter/sort/limit contract for world queries.
 */
public record WorldQuery(
    String schemaHash,
    Long createdAfterInclusive,
    Long createdBeforeInclusive,
    boolean sortCreatedAtDesc,
    int offset,
    int limit
) {
    public static WorldQuery defaults() {
        return new WorldQuery(null, null, null, true, 0, 0);
    }

    public int safeOffset() {
        return Math.max(0, offset);
    }

    public int safeLimit() {
        return Math.max(0, limit);
    }
}
