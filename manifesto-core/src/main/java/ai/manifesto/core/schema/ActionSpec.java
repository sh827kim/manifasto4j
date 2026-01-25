package ai.manifesto.core.schema;

import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.flow.FlowNode;

import java.util.*;

/**
 * ActionSpec - 액션(Action) 정의
 *
 * 각 액션의 메타데이터:
 * - 이름: actionId
 * - Input: 입력 파라미터 정의
 * - Available: 액션 실행 가능 조건
 * - Flow: 액션 실행할 Flow
 *
 * 특징:
 * - 불변 객체 (모든 필드 final)
 * - 빌더 패턴으로 생성
 */
public final class ActionSpec {
    private final String actionId;
    private final Map<String, FieldSpec> inputFields;  // 입력 파라미터 정의
    private final ExprNode available;                   // 실행 가능 조건
    private final FlowNode flow;                         // 실행 Flow

    public ActionSpec(
        String actionId,
        Map<String, FieldSpec> inputFields,
        ExprNode available,
        FlowNode flow
    ) {
        this.actionId = Objects.requireNonNull(actionId, "actionId required");
        this.inputFields = Collections.unmodifiableMap(
            new HashMap<>(inputFields != null ? inputFields : new HashMap<>())
        );
        this.available = available;
        this.flow = Objects.requireNonNull(flow, "flow required");
    }

    public String getActionId() {
        return actionId;
    }

    public Map<String, FieldSpec> getInputFields() {
        return inputFields;
    }

    public ExprNode getAvailable() {
        return available;
    }

    public FlowNode getFlow() {
        return flow;
    }

    /**
     * 액션 빌더
     */
    public static class Builder {
        private final String actionId;
        private final Map<String, FieldSpec> inputFields = new HashMap<>();
        private ExprNode available;
        private FlowNode flow;

        public Builder(String actionId) {
            this.actionId = Objects.requireNonNull(actionId);
        }

        public Builder addInputField(String name, FieldSpec field) {
            this.inputFields.put(name, field);
            return this;
        }

        public Builder available(ExprNode expr) {
            this.available = expr;
            return this;
        }

        public Builder flow(FlowNode flow) {
            this.flow = flow;
            return this;
        }

        public ActionSpec build() {
            return new ActionSpec(actionId, inputFields, available, flow);
        }
    }

    @Override
    public String toString() {
        return "ActionSpec{actionId='" + actionId + "'}";
    }
}
