package ai.manifesto.world.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * KR: PolicyRulesPolicy는 권한/거버넌스 정책 구성을 표현하는 값 객체입니다.
 * EN: PolicyRulesPolicy is a value object describing authority/governance policy configuration.
 */
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
