package ai.manifesto.core;

import java.util.List;
import java.util.Objects;

/**
 * KR: ExplainResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: ExplainResult is a result type carrying operation or execution outcomes.
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
