package ai.manifesto.bridge;

/**
 * SourceEvent - 외부 트리거 이벤트
 */
public record SourceEvent(
    Kind kind,
    String eventId,
    Object payload,
    Long occurredAt
) {
    public enum Kind { UI, API, AGENT, SYSTEM }
}
