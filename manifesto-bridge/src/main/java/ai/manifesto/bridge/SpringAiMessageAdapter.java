package ai.manifesto.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * SpringAiMessageAdapter normalizes Spring AI style input maps into SourceEvent.
 *
 * <p>Expected keys:
 * - type: chat | tool | system | api (optional, default: api)
 * - eventId: event identifier (optional, generated when missing)
 * - payload: message payload object (optional, default: empty map)
 * - occurredAt: epoch millis (optional)
 * - metadata: map to be merged into payload (optional)
 */
public final class SpringAiMessageAdapter implements ExternalEventAdapter<Map<String, Object>> {

    @Override
    public SourceEvent adapt(Map<String, Object> event) {
        Objects.requireNonNull(event, "event is required");

        SourceEvent.Kind kind = toKind(asString(event.get("type")));
        String eventId = normalizeEventId(asString(event.get("eventId")));
        Long occurredAt = asLong(event.get("occurredAt"));
        Map<String, Object> payload = normalizePayload(event);

        return new SourceEvent(kind, eventId, payload, occurredAt);
    }

    private Map<String, Object> normalizePayload(Map<String, Object> event) {
        Object payloadCandidate = event.get("payload");
        Map<String, Object> normalized = new HashMap<>();

        if (payloadCandidate instanceof Map<?, ?> payloadMap) {
            for (Map.Entry<?, ?> entry : payloadMap.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    normalized.put(key, entry.getValue());
                }
            }
        }

        Object metadataCandidate = event.get("metadata");
        if (metadataCandidate instanceof Map<?, ?> metadataMap) {
            for (Map.Entry<?, ?> entry : metadataMap.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    normalized.put("meta." + key, entry.getValue());
                }
            }
        }

        return Map.copyOf(normalized);
    }

    private SourceEvent.Kind toKind(String type) {
        if (type == null || type.isBlank()) {
            return SourceEvent.Kind.API;
        }
        return switch (type.trim().toLowerCase()) {
            case "chat", "ui" -> SourceEvent.Kind.UI;
            case "tool", "agent" -> SourceEvent.Kind.AGENT;
            case "system" -> SourceEvent.Kind.SYSTEM;
            case "api" -> SourceEvent.Kind.API;
            default -> throw new IllegalArgumentException("Unsupported Spring AI event type: " + type);
        };
    }

    private String normalizeEventId(String eventId) {
        if (eventId != null && !eventId.isBlank()) {
            return eventId;
        }
        return "spring-ai-" + UUID.randomUUID();
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new IllegalArgumentException("Expected string value but got: " + value.getClass().getSimpleName());
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("Expected number value but got: " + value.getClass().getSimpleName());
    }
}
