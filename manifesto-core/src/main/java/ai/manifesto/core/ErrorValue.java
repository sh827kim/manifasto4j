package ai.manifesto.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: ErrorValue는 Core 모듈에서 error value 역할을 수행하는 구현 타입입니다.
 * EN: ErrorValue is an implementation type performing error value roles in the Core module.
 */
public class ErrorValue {

    private final String code;                    // 에러 코드
    private final String message;                 // 사람이 읽을 수 있는 메시지
    private final ErrorSource source;             // 에러 발생 위치
    private final long timestamp;                 // 발생 시각 (ms)
    private final Map<String, Object> context;    // 추가 컨텍스트

    /**
     * 생성자
     */
    private ErrorValue(String code, String message, ErrorSource source,
                       long timestamp, Map<String, Object> context) {
        this.code = code;
        this.message = message;
        this.source = source;
        this.timestamp = timestamp;
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
    }

    /**
     * ErrorValue 생성 (빌더 패턴)
     */
    public static ErrorValue create(String code, String message,
                                    String actionId, String nodePath, long timestamp) {
        ErrorSource source = new ErrorSource(actionId, nodePath);
        return new ErrorValue(code, message, source, timestamp, null);
    }

    /**
     * ErrorValue 생성 (컨텍스트 포함)
     */
    public static ErrorValue createWithContext(String code, String message,
                                               String actionId, String nodePath,
                                               long timestamp,
                                               Map<String, Object> context) {
        ErrorSource source = new ErrorSource(actionId, nodePath);
        return new ErrorValue(code, message, source, timestamp, context);
    }

    // Getters
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public ErrorSource getSource() { return source; }
    public long getTimestamp() { return timestamp; }
    public Map<String, Object> getContext() { return new HashMap<>(context); }

    /**
     * JSON 표현
     */
    @Override
    public String toString() {
        return "ErrorValue{" +
               "code='" + code + '\'' +
               ", message='" + message + '\'' +
               ", source=" + source +
               ", timestamp=" + timestamp +
               ", context=" + context +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorValue that)) return false;
        return timestamp == that.timestamp &&
               Objects.equals(code, that.code) &&
               Objects.equals(message, that.message) &&
               Objects.equals(source, that.source) &&
               Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, source, timestamp, context);
    }

    /**
     * 에러 발생 위치
     */
    public static class ErrorSource {
        private final String actionId;    // 액션 식별자
        private final String nodePath;    // 노드 경로 (예: "actions.addTodo.flow.seq.0")

        public ErrorSource(String actionId, String nodePath) {
            this.actionId = actionId;
            this.nodePath = nodePath;
        }

        public String getActionId() { return actionId; }
        public String getNodePath() { return nodePath; }

        @Override
        public String toString() {
            return "ErrorSource{" +
                   "actionId='" + actionId + '\'' +
                   ", nodePath='" + nodePath + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ErrorSource that)) return false;
            return Objects.equals(actionId, that.actionId) &&
                   Objects.equals(nodePath, that.nodePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(actionId, nodePath);
        }
    }
}
