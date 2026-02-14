package ai.manifesto.world.persistence;

/**
 * KR: WorldStore 엔티티 카운트 통계를 나타냅니다.
 * EN: Entity count statistics for WorldStore.
 */
public record StoreStats(
    int worlds,
    int edges,
    int proposals,
    int decisions,
    int bindings,
    int snapshots
) {
}
