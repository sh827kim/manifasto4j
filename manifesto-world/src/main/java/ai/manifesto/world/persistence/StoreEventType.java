package ai.manifesto.world.persistence;

/**
 * KR: WorldStore 변경 이벤트 타입 집합입니다.
 * EN: Set of WorldStore mutation event types.
 */
public enum StoreEventType {
    WORLD_SAVED,
    EDGE_SAVED,
    PROPOSAL_SAVED,
    PROPOSAL_UPDATED,
    PROPOSAL_DELETED,
    DECISION_SAVED,
    BINDING_SAVED,
    BINDING_REMOVED,
    GENESIS_SET
}
