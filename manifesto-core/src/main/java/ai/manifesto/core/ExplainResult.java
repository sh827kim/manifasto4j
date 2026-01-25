package ai.manifesto.core;

import java.util.List;
import java.util.Objects;

/**
 * ExplainResult - Explain 함수의 결과
 *
 * 값이 왜 그렇게 계산되었는지를 설명한다.
 * 경로의 값, 계산 추적, 의존성을 포함한다.
 */
public class ExplainResult {

    private final Object value;              // 경로의 값
    private final TraceNode trace;           // 값이 어떻게 계산되었는지 보여주는 추적
    private final List<String> deps;         // 이 값에 영향을 주는 의존성들

    /**
     * ExplainResult 생성
     */
    public ExplainResult(Object value, TraceNode trace, List<String> deps) {
        this.value = value;
        this.trace = Objects.requireNonNull(trace, "trace is required");
        this.deps = Objects.requireNonNull(deps, "deps list is required");
    }

    // Getters
    public Object getValue() {
        return value;
    }

    public TraceNode getTrace() {
        return trace;
    }

    public List<String> getDeps() {
        return List.copyOf(deps);
    }

    @Override
    public String toString() {
        return "ExplainResult{" +
               "value=" + value +
               ", trace=" + trace.getId() +
               ", deps=" + deps.size() +
               "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExplainResult)) return false;
        ExplainResult that = (ExplainResult) o;
        return Objects.equals(value, that.value) &&
               Objects.equals(trace, that.trace) &&
               Objects.equals(deps, that.deps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, trace, deps);
    }
}
