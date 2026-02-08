package ai.manifesto.world.schema;

import java.util.Objects;

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
