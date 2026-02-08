package ai.manifesto.world.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PolicyRulesPolicy implements AuthorityPolicy {
    private final List<PolicyRule> rules;
    private final PolicyRuleDecision defaultDecision;
    private final AuthorityRef escalateTo;

    public PolicyRulesPolicy(List<PolicyRule> rules, PolicyRuleDecision defaultDecision, AuthorityRef escalateTo) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules != null ? rules : List.of()));
        this.defaultDecision = Objects.requireNonNull(defaultDecision, "defaultDecision is required");
        this.escalateTo = escalateTo;
    }

    public List<PolicyRule> getRules() {
        return rules;
    }

    public PolicyRuleDecision getDefaultDecision() {
        return defaultDecision;
    }

    public AuthorityRef getEscalateTo() {
        return escalateTo;
    }

    @Override
    public AuthorityPolicyMode getMode() {
        return AuthorityPolicyMode.POLICY_RULES;
    }
}
