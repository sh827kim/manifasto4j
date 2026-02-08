package ai.manifesto.bridge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SpringAiMessageAdapter 테스트")
class SpringAiMessageAdapterTest {

    @Test
    @DisplayName("chat 타입 입력을 UI SourceEvent로 정규화한다")
    void adaptsChatTypeToUiKind() {
        SpringAiMessageAdapter adapter = new SpringAiMessageAdapter();

        SourceEvent event = adapter.adapt(
            Map.of(
                "type", "chat",
                "eventId", "evt-chat-1",
                "occurredAt", 12345L,
                "payload", Map.of("message", "hello"),
                "metadata", Map.of("sessionId", "s-1")
            )
        );

        assertEquals(SourceEvent.Kind.UI, event.kind());
        assertEquals("evt-chat-1", event.eventId());
        assertEquals(12345L, event.occurredAt());
        assertEquals("hello", ((Map<?, ?>) event.payload()).get("message"));
        assertEquals("s-1", ((Map<?, ?>) event.payload()).get("meta.sessionId"));
    }

    @Test
    @DisplayName("eventId 누락 시 자동 생성한다")
    void generatesEventIdWhenMissing() {
        SpringAiMessageAdapter adapter = new SpringAiMessageAdapter();
        SourceEvent event = adapter.adapt(Map.of("type", "tool"));
        assertEquals(SourceEvent.Kind.AGENT, event.kind());
        assertNotNull(event.eventId());
    }

    @Test
    @DisplayName("지원하지 않는 type이면 예외를 던진다")
    void rejectsUnsupportedType() {
        SpringAiMessageAdapter adapter = new SpringAiMessageAdapter();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> adapter.adapt(Map.of("type", "unknown"))
        );

        assertEquals("Unsupported Spring AI event type: unknown", exception.getMessage());
    }
}
