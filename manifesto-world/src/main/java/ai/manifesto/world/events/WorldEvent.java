package ai.manifesto.world.events;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: WorldEvent는 World 이벤트 계층에서 world event 역할을 수행하는 구현 타입입니다.
 * EN: WorldEvent is an implementation type performing world event roles in the World event layer.
 */
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
