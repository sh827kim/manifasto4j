package ai.manifesto.world.events;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class WorldEvent {
    private final String type;
    private final long timestamp;
    private final Map<String, Object> payload;

    public WorldEvent(String type, long timestamp, Map<String, Object> payload) {
        this.type = Objects.requireNonNull(type, "type is required");
        this.timestamp = timestamp;
        this.payload = Collections.unmodifiableMap(new LinkedHashMap<>(payload != null ? payload : Map.of()));
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
