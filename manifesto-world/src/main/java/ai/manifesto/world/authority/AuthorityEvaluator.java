package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.Proposal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AuthorityEvaluator {
    private final Map<String, AuthorityHandler> handlers = new HashMap<>();

    private final AutoApproveHandler autoHandler = new AutoApproveHandler();
    private final HitlHandler hitlHandler = new HitlHandler();
    private final PolicyRulesHandler policyHandler = new PolicyRulesHandler();
    private final TribunalHandler tribunalHandler = new TribunalHandler();

    public AuthorityEvaluator() {
        handlers.put("AUTO_APPROVE", autoHandler);
        handlers.put("HITL", hitlHandler);
        handlers.put("POLICY_RULES", policyHandler);
        handlers.put("TRIBUNAL", tribunalHandler);
    }

    public AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding) {
        String mode = binding.getPolicy().getMode().name();
        AuthorityHandler handler = handlers.get(mode);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown policy mode: " + mode);
        }
        return handler.evaluate(proposal, binding);
    }

    public AuthorityResponse submitHitlDecision(String proposalId, String decision, String reasoning, ai.manifesto.world.schema.IntentScope approvedScope) {
        return hitlHandler.submitDecision(proposalId, decision, reasoning, approvedScope);
    }

    public AuthorityResponse submitTribunalVote(String proposalId, ai.manifesto.world.schema.ActorRef voter, TribunalHandler.VoteDecision decision, String reasoning) {
        return tribunalHandler.submitVote(proposalId, voter, decision, reasoning);
    }

    public AutoApproveHandler getAutoHandler() {
        return autoHandler;
    }

    public HitlHandler getHitlHandler() {
        return hitlHandler;
    }

    public PolicyRulesHandler getPolicyHandler() {
        return policyHandler;
    }

    public TribunalHandler getTribunalHandler() {
        return tribunalHandler;
    }

    public List<AuthorityDecisionEvent> resolveTimeouts(long nowMillis) {
        List<AuthorityDecisionEvent> decisions = new ArrayList<>();
        decisions.addAll(hitlHandler.resolveTimeouts(nowMillis));
        decisions.addAll(tribunalHandler.resolveTimeouts(nowMillis));
        return decisions;
    }

    public void dropPending(Set<String> proposalIds) {
        hitlHandler.dropPending(proposalIds);
        tribunalHandler.dropPending(proposalIds);
    }
}
