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
    private final FieldSpec inputSpec;                 // 입력 스키마 (object)
    private final ExprNode available;                   // 실행 가능 조건
    private final FlowNode flow;                         // 실행 Flow
    private final String description;

    public ActionSpec(
        String actionId,
        Map<String, FieldSpec> inputFields,
        FieldSpec inputSpec,
        ExprNode available,
        FlowNode flow,
        String description
    ) {
        this.actionId = Objects.requireNonNull(actionId, "actionId required");
        Map<String, FieldSpec> normalizedFields = new HashMap<>();
        if (inputFields != null) {
            normalizedFields.putAll(inputFields);
        }
        FieldSpec normalizedSpec = inputSpec;
        if (normalizedSpec == null && !normalizedFields.isEmpty()) {
            normalizedSpec = new FieldSpec(
                "input",
                "object",
                true,
                null,
                normalizedFields,
                null,
                null,
                null
            );
        }
        if (normalizedSpec != null && (normalizedFields == null || normalizedFields.isEmpty())) {
            if ("object".equals(normalizedSpec.getType()) && normalizedSpec.getFields() != null) {
                normalizedFields.putAll(normalizedSpec.getFields());
            }
        }
        this.inputFields = Collections.unmodifiableMap(new HashMap<>(normalizedFields));
        this.inputSpec = normalizedSpec;
        this.available = available;
        this.flow = Objects.requireNonNull(flow, "flow required");
        this.description = description;
    }

    public String getActionId() {
        return actionId;
    }

    public Map<String, FieldSpec> getInputFields() {
        return inputFields;
    }

    public FieldSpec getInputSpec() {
        return inputSpec;
    }

    public ExprNode getAvailable() {
        return available;
    }

    public FlowNode getFlow() {
        return flow;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 액션 빌더
     */
    public static class Builder {
        private final String actionId;
        private final Map<String, FieldSpec> inputFields = new HashMap<>();
        private FieldSpec inputSpec;
        private ExprNode available;
        private FlowNode flow;
        private String description;

        public Builder(String actionId) {
            this.actionId = Objects.requireNonNull(actionId);
        }

        public Builder addInputField(String name, FieldSpec field) {
            this.inputFields.put(name, field);
            return this;
        }

        public Builder inputSpec(FieldSpec inputSpec) {
            this.inputSpec = inputSpec;
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

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ActionSpec build() {
            return new ActionSpec(actionId, inputFields, inputSpec, available, flow, description);
        }
    }

    @Override
    public String toString() {
        return "ActionSpec{actionId='" + actionId + "'}";
    }
}
