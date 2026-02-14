package ai.manifesto.world.persistence;

/**
 * KR: WorldStore observable 이벤트 payload입니다.
 * EN: Observable event payload emitted by WorldStore implementations.
 */
public record StoreEvent(
    StoreEventType type,
    long timestamp,
    Object data
) {
}
