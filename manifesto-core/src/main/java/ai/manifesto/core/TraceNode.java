package ai.manifesto.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TraceNode - 계산 추적 노드
 * 계산의 각 단계를 기록하여 설명 가능성(explainability)을 제공한다.
 *
 * 모든 연산은 TraceNode로 기록된다:
 * - expr: 표현식 평가
 * - flow: Flow 노드 실행
 * - patch: 상태 변경
 * - effect: 효과 선언
 * - error: 에러 발생
 * - branch: 분기 선택
 * - call: Flow 호출
 * - halt: 정상 중단
 *
 * Java 17+ 기능: Enum은 sealed이고 불변이다
 */
public class TraceNode {

    /**
     * TraceNodeKind - 노드의 종류
     *
     * sealed enum처럼 동작한다 (Java 17+에서 공식 sealed enum 지원 예정)
     */
    public enum Kind {
        EXPR("expr"),           // 표현식 평가
        COMPUTED("computed"),   // 계산된 값 평가
        FLOW("flow"),           // Flow 실행
        PATCH("patch"),         // 상태 변경
        EFFECT("effect"),       // 효과 선언
        BRANCH("branch"),       // 분기 선택
        CALL("call"),           // Flow 호출
        HALT("halt"),           // 정상 중단
        ERROR("error");         // 에러 발생

        private final String code;

        Kind(String code) {
            this.code = code;
        }

        public String getCode() { return code; }

        public static Kind fromCode(String code) {
            for (Kind kind : values()) {
                if (kind.code.equals(code)) return kind;
            }
            throw new IllegalArgumentException("Unknown kind: " + code);
        }
    }

    private final String id;                      // 고유 식별자
    private final Kind kind;                      // 노드 종류
    private final String sourcePath;              // 스키마 경로 (예: "actions.addTodo.flow.seq.0")
    private final Map<String, Object> inputs;     // 입력값
    private final Object output;                  // 출력값
    private final List<TraceNode> children;       // 자식 노드
    private final long timestamp;                 // 실행 시각

    /**
     * 생성자
     */
    private TraceNode(String id, Kind kind, String sourcePath,
                     Map<String, Object> inputs, Object output,
                     List<TraceNode> children, long timestamp) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.kind = Objects.requireNonNull(kind, "kind is required");
        this.sourcePath = sourcePath;
        this.inputs = new HashMap<>(inputs != null ? inputs : new HashMap<>());
        this.output = output;
        this.children = new ArrayList<>(children != null ? children : new ArrayList<>());
        this.timestamp = timestamp;
    }

    // ===== Getters =====
    public String getId() { return id; }
    public Kind getKind() { return kind; }
    public String getSourcePath() { return sourcePath; }
    public Map<String, Object> getInputs() { return new HashMap<>(inputs); }
    public Object getOutput() { return output; }
    public List<TraceNode> getChildren() { return new ArrayList<>(children); }
    public long getTimestamp() { return timestamp; }

    /**
     * 빌더 - Java 17+ 레코드처럼 사용 가능
     */
    public static class Builder {
        private String id;
        private Kind kind;
        private String sourcePath;
        private Map<String, Object> inputs = new HashMap<>();
        private Object output;
        private List<TraceNode> children = new ArrayList<>();
        private long timestamp;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder kind(Kind kind) {
            this.kind = kind;
            return this;
        }

        public Builder sourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public Builder inputs(Map<String, Object> inputs) {
            this.inputs = new HashMap<>(inputs);
            return this;
        }

        public Builder input(String key, Object value) {
            this.inputs.put(key, value);
            return this;
        }

        public Builder output(Object output) {
            this.output = output;
            return this;
        }

        public Builder children(List<TraceNode> children) {
            this.children = new ArrayList<>(children);
            return this;
        }

        public Builder addChild(TraceNode child) {
            this.children.add(child);
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TraceNode build() {
            return new TraceNode(id, kind, sourcePath, inputs, output,
                children, timestamp);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "TraceNode{" +
               "id='" + id + '\'' +
               ", kind=" + kind +
               ", sourcePath='" + sourcePath + '\'' +
               ", childCount=" + children.size() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TraceNode traceNode)) return false;
        return timestamp == traceNode.timestamp &&
               Objects.equals(id, traceNode.id) &&
               kind == traceNode.kind &&
               Objects.equals(sourcePath, traceNode.sourcePath) &&
               Objects.equals(inputs, traceNode.inputs) &&
               Objects.equals(output, traceNode.output) &&
               Objects.equals(children, traceNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, kind, sourcePath, inputs, output, children,
            timestamp);
    }
}
