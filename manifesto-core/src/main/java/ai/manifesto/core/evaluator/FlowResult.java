package ai.manifesto.core.evaluator;

import ai.manifesto.core.TraceNode;

import java.util.Objects;

/**
 * KR: FlowResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: FlowResult is a result type carrying operation or execution outcomes.
 */
public record FlowResult(FlowState state, TraceNode trace) {

    public FlowResult {
        Objects.requireNonNull(state, "state is required");
        Objects.requireNonNull(trace, "trace is required");
    }

    /**
     * 결과 요약
     */
    @Override
    public String toString() {
        return "FlowResult{" +
               "status=" + state.getStatus() +
               ", trace=" + trace.getKind() +
               '}';
    }
}
