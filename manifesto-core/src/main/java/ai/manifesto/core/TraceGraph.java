package ai.manifesto.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KR: TraceGraph는 Core 모듈에서 trace graph 역할을 수행하는 구현 타입입니다.
 * EN: TraceGraph is an implementation type performing trace graph roles in the Core module.
 */
public class TraceGraph {

    private final TraceNode root;                  // 루트 노드
    private final Map<String, TraceNode> nodes;   // 모든 노드 인덱스
    private final Intent intent;                   // 트리거 Intent
    private final long baseVersion;               // 시작 Snapshot 버전
    private final long resultVersion;             // 종료 Snapshot 버전
    private final long duration;                  // 실행 시간 (ms)
    private final TraceTermination terminatedBy;  // 종료 원인

    /**
     * 생성자
     */
    private TraceGraph(TraceNode root, Map<String, TraceNode> nodes,
                      Intent intent, long baseVersion, long resultVersion,
                      long duration, TraceTermination terminatedBy) {
        this.root = root;
        this.nodes = new HashMap<>(nodes != null ? nodes : new HashMap<>());
        this.intent = intent;
        this.baseVersion = baseVersion;
        this.resultVersion = resultVersion;
        this.duration = duration;
        this.terminatedBy = terminatedBy;
    }

    // Getters
    public TraceNode getRoot() { return root; }
    public Map<String, TraceNode> getNodes() { return new HashMap<>(nodes); }
    public Intent getIntent() { return intent; }
    public long getBaseVersion() { return baseVersion; }
    public long getResultVersion() { return resultVersion; }
    public long getDuration() { return duration; }
    public TraceTermination getTerminatedBy() { return terminatedBy; }

    /**
     * 빌더
     */
    public static class Builder {
        private TraceNode root;
        private Map<String, TraceNode> nodes = new HashMap<>();
        private Intent intent;
        private long baseVersion;
        private long resultVersion;
        private long duration;
        private TraceTermination terminatedBy = TraceTermination.COMPLETE;

        public Builder root(TraceNode root) {
            this.root = root;
            return this;
        }

        public Builder nodes(Map<String, TraceNode> nodes) {
            this.nodes = new HashMap<>(nodes);
            return this;
        }

        public Builder addNode(TraceNode node) {
            this.nodes.put(node.getId(), node);
            return this;
        }

        public Builder intent(Intent intent) {
            this.intent = intent;
            return this;
        }

        public Builder baseVersion(long baseVersion) {
            this.baseVersion = baseVersion;
            return this;
        }

        public Builder resultVersion(long resultVersion) {
            this.resultVersion = resultVersion;
            return this;
        }

        public Builder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder terminatedBy(TraceTermination terminatedBy) {
            this.terminatedBy = terminatedBy;
            return this;
        }

        public TraceGraph build() {
            return new TraceGraph(root, nodes, intent, baseVersion,
                resultVersion, duration, terminatedBy);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "TraceGraph{" +
               "intent=" + intent.getType() +
               ", baseVersion=" + baseVersion +
               ", resultVersion=" + resultVersion +
               ", duration=" + duration + "ms" +
               ", terminatedBy=" + terminatedBy +
               ", nodeCount=" + nodes.size() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TraceGraph that)) return false;
        return baseVersion == that.baseVersion &&
               resultVersion == that.resultVersion &&
               duration == that.duration &&
               Objects.equals(root, that.root) &&
               Objects.equals(nodes, that.nodes) &&
               Objects.equals(intent, that.intent) &&
               terminatedBy == that.terminatedBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, nodes, intent, baseVersion, resultVersion,
            duration, terminatedBy);
    }

    /**
     * TraceTermination - Flow 종료 원인
     */
    public enum TraceTermination {
        COMPLETE("complete"),   // 모든 노드 완료
        EFFECT("effect"),        // 효과 발견 (펼딩)
        HALT("halt"),           // 정상 중단
        ERROR("error");         // 에러

        private final String code;

        TraceTermination(String code) {
            this.code = code;
        }

        public String getCode() { return code; }
    }

    /**
     * TraceIdGenerator - 결정론적 ID 생성
     * 같은 타임스탐프에서는 같은 ID를 생성한다.
     */
    public static class TraceIdGenerator {
        private final long timestamp;
        private final AtomicInteger counter = new AtomicInteger(0);

        public TraceIdGenerator(long timestamp) {
            this.timestamp = timestamp;
        }

        public String nextId() {
            int count = counter.getAndIncrement();
            return String.format("trace_%d_%d", timestamp, count);
        }
    }
}
