package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: IntentSource는 World 스키마 계층에서 intent source 역할을 수행하는 구현 타입입니다.
 * EN: IntentSource is an implementation type performing intent source roles in the World schema layer.
 */
public final class IntentSource {
    private final String kind;
    private final String eventId;

    public IntentSource(String kind, String eventId) {
        this.kind = Objects.requireNonNull(kind, "kind is required");
        this.eventId = Objects.requireNonNull(eventId, "eventId is required");
    }

    public String getKind() {
        return kind;
    }

    public String getEventId() {
        return eventId;
    }
}
