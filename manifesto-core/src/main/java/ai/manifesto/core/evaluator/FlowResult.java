package ai.manifesto.core.evaluator;

import ai.manifesto.core.TraceNode;

import java.util.Objects;

/**
 * FlowResult - Flow 평가의 결과
 *
 * Flow 평가 후의 상태와 추적 정보를 함께 반환한다:
 * - state: 평가 후의 FlowState
 * - trace: 이 Flow 노드의 추적 정보
 *
 * 예: FlowEvaluator.evaluate()의 반환 타입
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
