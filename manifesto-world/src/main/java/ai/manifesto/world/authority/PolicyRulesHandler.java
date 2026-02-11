package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.PolicyCondition;
import ai.manifesto.world.schema.PolicyConditionKind;
import ai.manifesto.world.schema.PolicyRule;
import ai.manifesto.world.schema.PolicyRuleDecision;
import ai.manifesto.world.schema.PolicyRulesPolicy;
import ai.manifesto.world.schema.Proposal;

import java.util.HashMap;
import java.util.Map;

/**
 * KR: PolicyRulesHandler는 특정 도메인 이벤트/요청을 처리하는 핸들러 타입입니다.
 * EN: PolicyRulesHandler is a handler type that processes specific domain events or requests.
 */
public final class PolicyRulesHandler implements AuthorityHandler {
    @FunctionalInterface
    public interface CustomConditionEvaluator {
        boolean evaluate(Proposal proposal, ActorAuthorityBinding binding);
    }

    private final Map<String, CustomConditionEvaluator> customEvaluators = new HashMap<>();

    public void registerCustomEvaluator(String name, CustomConditionEvaluator evaluator) {
        customEvaluators.put(name, evaluator);
    }

    @Override
    public AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding) {
        if (!(binding.getPolicy() instanceof PolicyRulesPolicy policy)) {
            throw new IllegalArgumentException("PolicyRulesHandler requires POLICY_RULES policy");
        }

        for (PolicyRule rule : policy.getRules()) {
            if (evaluateCondition(rule.getCondition(), proposal, binding)) {
                return applyDecision(rule.getDecision(), rule.getReason(), proposal, policy);
            }
        }

        return applyDecision(policy.getDefaultDecision(), "Default policy decision", proposal, policy);
    }

    private boolean evaluateCondition(PolicyCondition condition, Proposal proposal, ActorAuthorityBinding binding) {
        if (condition.getKind() == PolicyConditionKind.INTENT_TYPE) {
            return condition.getTypes().contains(proposal.getIntent().getBody().getType());
        }
        if (condition.getKind() == PolicyConditionKind.SCOPE_PATTERN) {
            return matchPattern(proposal.getIntent().getBody().getType(), condition.getPattern());
        }
        if (condition.getKind() == PolicyConditionKind.CUSTOM) {
            CustomConditionEvaluator evaluator = customEvaluators.get(condition.getEvaluator());
            return evaluator != null && evaluator.evaluate(proposal, binding);
        }
        return false;
    }

    private AuthorityResponse applyDecision(
            PolicyRuleDecision decision,
            String reason,
            Proposal proposal,
            PolicyRulesPolicy policy
    ) {
        if (decision == PolicyRuleDecision.APPROVE) {
            return AuthorityResponse.approved(proposal.getIntent().getBody().getScopeProposal());
        }
        if (decision == PolicyRuleDecision.REJECT) {
            return AuthorityResponse.rejected(reason != null ? reason : "Policy rejection");
        }
        if (policy.getEscalateTo() != null) {
            return AuthorityResponse.rejected(
                    "ESCALATE:" + policy.getEscalateTo().getAuthorityId()
            );
        }
        return AuthorityResponse.rejected(reason != null ? reason : "Policy escalation required");
    }

    private boolean matchPattern(String value, String pattern) {
        if (pattern == null) {
            return false;
        }
        String regex = pattern.replace("*", ".*").replace("?", ".");
        return value.matches("^" + regex + "$");
    }
}
