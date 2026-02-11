package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.HitlPolicy;
import ai.manifesto.world.schema.IntentScope;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.TimeoutAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * KR: HitlHandler는 특정 도메인 이벤트/요청을 처리하는 핸들러 타입입니다.
 * EN: HitlHandler is a handler type that processes specific domain events or requests.
 */
public final class HitlHandler implements AuthorityHandler {
    public static final class PendingDecision {
        private final Proposal proposal;
        private final ActorAuthorityBinding binding;
        private final HitlPolicy policy;
        private final long enteredAtMillis;

        private PendingDecision(Proposal proposal, ActorAuthorityBinding binding, HitlPolicy policy, long enteredAtMillis) {
            this.proposal = proposal;
            this.binding = binding;
            this.policy = policy;
            this.enteredAtMillis = enteredAtMillis;
        }

        public Proposal getProposal() {
            return proposal;
        }

        public ActorAuthorityBinding getBinding() {
            return binding;
        }

        public HitlPolicy getPolicy() {
            return policy;
        }

        public long getEnteredAtMillis() {
            return enteredAtMillis;
        }
    }

    private final Map<String, PendingDecision> pendingDecisions = new LinkedHashMap<>();

    @Override
    public AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding) {
        if (!(binding.getPolicy() instanceof HitlPolicy policy)) {
            throw new IllegalArgumentException("HitlHandler requires HITL policy");
        }
        String proposalId = proposal.getProposalId().value();
        if (pendingDecisions.containsKey(proposalId)) {
            throw new IllegalStateException("Proposal already pending HITL: " + proposalId);
        }
        pendingDecisions.put(proposalId, new PendingDecision(proposal, binding, policy, System.currentTimeMillis()));
        return AuthorityResponse.pending(AuthorityResponse.WaitingFor.human(policy.getDelegate()));
    }

    public AuthorityResponse submitDecision(String proposalId, String decision, String reasoning, IntentScope approvedScope) {
        PendingDecision pendingDecision = pendingDecisions.remove(proposalId);
        if (pendingDecision == null) {
            throw new IllegalArgumentException("No pending HITL decision for proposal: " + proposalId);
        }

        if ("approved".equals(decision)) {
            IntentScope scope = approvedScope != null
                    ? approvedScope
                    : pendingDecision.getProposal().getIntent().getBody().getScopeProposal();
            return AuthorityResponse.approved(scope);
        }

        return AuthorityResponse.rejected(reasoning != null ? reasoning : "Human rejected");
    }

    public boolean isPending(String proposalId) {
        return pendingDecisions.containsKey(proposalId);
    }

    public List<AuthorityDecisionEvent> resolveTimeouts(long nowMillis) {
        List<AuthorityDecisionEvent> decisions = new ArrayList<>();
        for (Map.Entry<String, PendingDecision> entry : List.copyOf(pendingDecisions.entrySet())) {
            PendingDecision pending = entry.getValue();
            Long timeoutMillis = pending.getPolicy().getTimeoutMillis();
            if (timeoutMillis == null || timeoutMillis < 0) {
                continue;
            }

            if (nowMillis - pending.getEnteredAtMillis() < timeoutMillis) {
                continue;
            }

            pendingDecisions.remove(entry.getKey());
            TimeoutAction timeoutAction = pending.getPolicy().getOnTimeout();
            if (timeoutAction == TimeoutAction.APPROVE) {
                decisions.add(new AuthorityDecisionEvent(
                        entry.getKey(),
                        AuthorityResponse.approved(pending.getProposal().getIntent().getBody().getScopeProposal()),
                        "HITL timeout approve"
                ));
            } else {
                decisions.add(new AuthorityDecisionEvent(
                        entry.getKey(),
                        AuthorityResponse.rejected("HITL timeout reject"),
                        "HITL timeout reject"
                ));
            }
        }
        return decisions;
    }

    public void dropPending(Set<String> proposalIds) {
        for (String proposalId : proposalIds) {
            pendingDecisions.remove(proposalId);
        }
    }

    public void clearPending() {
        pendingDecisions.clear();
    }
}
