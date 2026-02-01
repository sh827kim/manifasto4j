package ai.manifesto.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Intent - 액션 수행 요청
 * 사용자가 애플리케이션에 무엇을 하고 싶은지 나타낸다.
 *
 * 핵심:
 * - type: 어떤 액션을 수행할 것인가 (예: "addTodo", "toggleTodo")
 * - input: 액션에 필요한 입력값 (예: { "title": "Buy milk" })
 * - intentId: 이 요청의 고유 식별자 (멱등성 보장)
 */
public class Intent {

    private final String type;                    // 액션 타입
    private final Map<String, Object> input;      // 입력 파라미터
    private final String intentId;                // 고유 식별자 (필수)

    /**
     * 생성자
     */
    public Intent(String type, Map<String, Object> input, String intentId) {
        this.type = Objects.requireNonNull(type, "type is required");
        this.input = input != null ? new HashMap<>(input) : new HashMap<>();
        this.intentId = Objects.requireNonNull(intentId, "intentId is required");
    }

    /**
     * 편의 생성자 (input 없음)
     */
    public Intent(String type, String intentId) {
        this(type, new HashMap<>(), intentId);
    }

    // Getters
    public String getType() { return type; }
    public Map<String, Object> getInput() { return new HashMap<>(input); }
    public String getIntentId() { return intentId; }

    /**
     * Intent 빌더
     */
    public static class Builder {
        private String type;
        private Map<String, Object> input = new HashMap<>();
        private String intentId;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder input(String key, Object value) {
            this.input.put(key, value);
            return this;
        }

        public Builder input(Map<String, Object> input) {
            this.input = new HashMap<>(input);
            return this;
        }

        public Builder intentId(String intentId) {
            this.intentId = intentId;
            return this;
        }

        public Intent build() {
            String resolvedIntentId = intentId == null ? "" : intentId;
            return new Intent(type, input, resolvedIntentId);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * TS createIntent 대응 헬퍼
     */
    public static Intent createIntent(String type) {
        return new Intent(type, Map.of(), "");
    }

    public static Intent createIntent(String type, String intentId) {
        return new Intent(type, intentId);
    }

    public static Intent createIntent(String type, Map<String, Object> input) {
        return new Intent(type, input, "");
    }

    public static Intent createIntent(String type, Map<String, Object> input, String intentId) {
        return new Intent(type, input, intentId);
    }

    @Override
    public String toString() {
        return "Intent{" +
               "type='" + type + '\'' +
               ", input=" + input +
               ", intentId='" + intentId + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Intent intent)) return false;
        return Objects.equals(type, intent.type) &&
               Objects.equals(input, intent.input) &&
               Objects.equals(intentId, intent.intentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, input, intentId);
    }
}
