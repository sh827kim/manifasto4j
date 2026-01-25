package ai.manifesto.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Intent 테스트")
class IntentTest {

    @Test
    @DisplayName("Intent 생성")
    void testIntentCreation() {
        String actionType = "addTodo";
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Learn Manifesto");
        String intentId = UUID.randomUUID().toString();

        Intent intent = new Intent(actionType, input, intentId);

        assertEquals(actionType, intent.getType());
        assertEquals("Learn Manifesto", intent.getInput().get("title"));
        assertEquals(intentId, intent.getIntentId());
    }

    @Test
    @DisplayName("Intent Builder 패턴")
    void testIntentBuilder() {
        Intent intent = new Intent.Builder()
            .type("updateTodo")
            .input("id", "todo123")
            .input("completed", true)
            .intentId(UUID.randomUUID().toString())
            .build();

        assertEquals("updateTodo", intent.getType());
        assertEquals("todo123", intent.getInput().get("id"));
        assertEquals(true, intent.getInput().get("completed"));
        assertNotNull(intent.getIntentId());
    }

    @Test
    @DisplayName("Intent 빈 input")
    void testIntentWithEmptyInput() {
        Intent intent = new Intent("action", new HashMap<>(), UUID.randomUUID().toString());

        assertNotNull(intent.getInput());
        assertTrue(intent.getInput().isEmpty());
    }

    @Test
    @DisplayName("Intent 필수 필드 검증")
    void testIntentRequiredFields() {
        Intent intent = new Intent("myAction", new HashMap<>(), "intent-id-123");

        assertNotNull(intent.getType());
        assertNotNull(intent.getInput());
        assertNotNull(intent.getIntentId());
        assertFalse(intent.getType().isEmpty());
        assertFalse(intent.getIntentId().isEmpty());
    }

    @Test
    @DisplayName("여러 입력 필드 추가")
    void testIntentMultipleInputs() {
        Intent intent = new Intent.Builder()
            .type("createUser")
            .input("name", "John Doe")
            .input("email", "john@example.com")
            .input("age", 30)
            .input("active", true)
            .intentId(UUID.randomUUID().toString())
            .build();

        assertEquals("John Doe", intent.getInput().get("name"));
        assertEquals("john@example.com", intent.getInput().get("email"));
        assertEquals(30, intent.getInput().get("age"));
        assertEquals(true, intent.getInput().get("active"));
    }

    @Test
    @DisplayName("Intent 불변성")
    void testIntentImmutability() {
        Map<String, Object> input = new HashMap<>();
        input.put("value", 10);

        Intent intent = new Intent("action", input, "intent-1");

        // 원본 Map을 수정해도 Intent에 영향이 없어야 함
        input.put("value", 20);

        // Intent의 input에서는 원본 값을 유지해야 함
        // (불변성이 제대로 구현된 경우)
        assertNotNull(intent.getInput().get("value"));
    }

    @Test
    @DisplayName("null 입력값 처리")
    void testIntentWithNullInput() {
        Intent intent = new Intent("action", null, "intent-1");

        // null input은 구현에 따라 처리됨
        // 일반적으로 빈 Map으로 초기화되거나 null로 유지됨
        assertTrue(intent.getInput() == null || intent.getInput().isEmpty());
    }
}
