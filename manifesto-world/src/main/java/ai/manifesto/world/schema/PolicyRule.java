package ai.manifesto.world.schema;

import java.util.Objects;

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
