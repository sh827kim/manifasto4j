package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: PolicyRule는 World 스키마 계층에서 policy rule 역할을 수행하는 구현 타입입니다.
 * EN: PolicyRule is an implementation type performing policy rule roles in the World schema layer.
 */
public final class PolicyRule {
    private final PolicyCondition condition;
    private final PolicyRuleDecision decision;
    private final String reason;

    public PolicyRule(PolicyCondition condition, PolicyRuleDecision decision, String reason) {
        this.condition = Objects.requireNonNull(condition, "condition is required");
        this.decision = Objects.requireNonNull(decision, "decision is required");
        this.reason = reason;
    }

    public PolicyCondition getCondition() {
        return condition;
    }

    public PolicyRuleDecision getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }
}
