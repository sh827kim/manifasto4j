package ai.manifesto.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Requirement - Host가 처리해야 할 효과 요구사항
 * Flow 평가 중 effect 노드를 만나면 Requirement가 생성된다.
 *
 * 필드:
 * - id: 요구사항 고유 식별자 (멱등성)
 * - type: 효과 타입 (예: "api.saveTodo", "file.write")
 * - params: 효과에 필요한 파라미터
 * - actionId: 이 요구사항을 생성한 액션
 * - flowPosition: Flow에서의 위치 (추적용)
 * - createdAt: 생성 시각
 */
public class Requirement {

    private final String id;
    private final String type;
    private final Map<String, Object> params;
    private final String actionId;
    private final FlowPosition flowPosition;
    private final long createdAt;

    /**
     * 생성자
     */
    private Requirement(String id, String type, Map<String, Object> params,
                        String actionId, FlowPosition flowPosition, long createdAt) {
        this.id = id;
        this.type = type;
        this.params = new HashMap<>(params != null ? params : new HashMap<>());
        this.actionId = actionId;
        this.flowPosition = flowPosition;
        this.createdAt = createdAt;
    }

    /**
     * Requirement 생성 (helpe 메서드)
     */
    public static Requirement create(String type, Map<String, Object> params,
                                     String schemaHash, String intentId,
                                     String actionId, String nodePath, long createdAt) {
        // id는 결정론적으로 생성 (재실행 시 같은 id)
        String id = generateDeterministicId(schemaHash, intentId, actionId, nodePath);

        FlowPosition position = new FlowPosition(nodePath, 0); // version은 나중에 설정
        return new Requirement(id, type, params, actionId, position, createdAt);
    }

    /**
     * 결정론적 ID 생성 (테스트용, 실제로는 해시 함수 사용)
     */
    private static String generateDeterministicId(
        String schemaHash,
        String intentId,
        String actionId,
        String nodePath
    ) {
        String input = safe(schemaHash) + ":" + safe(intentId) + ":" + safe(actionId) + ":" + safe(nodePath);
        String hash = sha256Hex(input);
        return "req-" + hash.substring(0, 16);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public Map<String, Object> getParams() { return new HashMap<>(params); }
    public String getActionId() { return actionId; }
    public FlowPosition getFlowPosition() { return flowPosition; }
    public long getCreatedAt() { return createdAt; }

    /**
     * Requirement 빌더
     */
    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private String type;
        private Map<String, Object> params = new HashMap<>();
        private String actionId;
        private FlowPosition flowPosition;
        private long createdAt = System.currentTimeMillis();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.params = new HashMap<>(params);
            return this;
        }

        public Builder param(String key, Object value) {
            this.params.put(key, value);
            return this;
        }

        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder flowPosition(FlowPosition flowPosition) {
            this.flowPosition = flowPosition;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Requirement build() {
            return new Requirement(id, type, params, actionId, flowPosition,
                createdAt);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Requirement{" +
               "id='" + id + '\'' +
               ", type='" + type + '\'' +
               ", actionId='" + actionId + '\'' +
               ", params=" + params +
               ", flowPosition=" + flowPosition +
               ", createdAt=" + createdAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Requirement that)) return false;
        return createdAt == that.createdAt &&
               Objects.equals(id, that.id) &&
               Objects.equals(type, that.type) &&
               Objects.equals(params, that.params) &&
               Objects.equals(actionId, that.actionId) &&
               Objects.equals(flowPosition, that.flowPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, params, actionId, flowPosition, createdAt);
    }

    /**
     * Flow 위치 정보
     */
    public static class FlowPosition {
        private final String nodePath;    // Flow 노드 경로
        private final long snapshotVersion;  // 이 시점의 snapshot 버전

        public FlowPosition(String nodePath, long snapshotVersion) {
            this.nodePath = nodePath;
            this.snapshotVersion = snapshotVersion;
        }

        public String getNodePath() { return nodePath; }
        public long getSnapshotVersion() { return snapshotVersion; }

        @Override
        public String toString() {
            return "FlowPosition{" +
                   "nodePath='" + nodePath + '\'' +
                   ", snapshotVersion=" + snapshotVersion +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FlowPosition that)) return false;
            return snapshotVersion == that.snapshotVersion &&
                   Objects.equals(nodePath, that.nodePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodePath, snapshotVersion);
        }
    }
}
