package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.QuorumKind;
import ai.manifesto.world.schema.TimeoutAction;
import ai.manifesto.world.schema.TribunalPolicy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TribunalHandler implements AuthorityHandler {
    public enum VoteDecision {
        APPROVE,
        REJECT,
        ABSTAIN
    }

    private static final class PendingTribunal {
        private final Proposal proposal;
        private final TribunalPolicy policy;
        private final Map<String, VoteDecision> votes = new LinkedHashMap<>();
        private final long enteredAtMillis;

        private PendingTribunal(Proposal proposal, TribunalPolicy policy, long enteredAtMillis) {
            this.proposal = proposal;
            this.policy = policy;
            this.enteredAtMillis = enteredAtMillis;
        }
    }

    private final Map<String, PendingTribunal> pending = new LinkedHashMap<>();

    @Override
    public AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding) {
        if (!(binding.getPolicy() instanceof TribunalPolicy policy)) {
            throw new IllegalArgumentException("TribunalHandler requires TRIBUNAL policy");
        }

        String proposalId = proposal.getProposalId().value();
        if (pending.containsKey(proposalId)) {
            throw new IllegalStateException("Proposal already pending tribunal: " + proposalId);
        }

        pending.put(proposalId, new PendingTribunal(proposal, policy, System.currentTimeMillis()));
        return AuthorityResponse.pending(AuthorityResponse.WaitingFor.tribunal(policy.getMembers()));
    }

    public AuthorityResponse submitVote(String proposalId, ActorRef voter, VoteDecision decision, String reasoning) {
        PendingTribunal tribunal = pending.get(proposalId);
        if (tribunal == null) {
            throw new IllegalArgumentException("No pending tribunal for proposal: " + proposalId);
        }

        boolean isMember = tribunal.policy.getMembers().stream().anyMatch(member -> member.getActorId().equals(voter.getActorId()));
        if (!isMember) {
            throw new IllegalArgumentException("Voter is not tribunal member: " + voter.getActorId());
        }

        if (tribunal.votes.containsKey(voter.getActorId())) {
            throw new IllegalArgumentException("Voter already voted: " + voter.getActorId());
        }

        tribunal.votes.put(voter.getActorId(), decision);

        int memberCount = tribunal.policy.getMembers().size();
        int approve = (int) tribunal.votes.values().stream().filter(v -> v == VoteDecision.APPROVE).count();
        int reject = (int) tribunal.votes.values().stream().filter(v -> v == VoteDecision.REJECT).count();

        boolean complete = false;
        boolean approved = false;

        if (tribunal.policy.getQuorum().getKind() == QuorumKind.UNANIMOUS) {
            if (approve == memberCount) {
                complete = true;
                approved = true;
            } else if (reject > 0 || tribunal.votes.size() == memberCount) {
                complete = true;
                approved = false;
            }
        } else if (tribunal.policy.getQuorum().getKind() == QuorumKind.MAJORITY) {
            int majority = (memberCount / 2) + 1;
            if (approve >= majority) {
                complete = true;
                approved = true;
            } else if (reject >= majority || tribunal.votes.size() == memberCount) {
                complete = true;
                approved = approve > reject;
            }
        } else {
            int threshold = tribunal.policy.getQuorum().getCount() != null ? tribunal.policy.getQuorum().getCount() : memberCount;
            if (approve >= threshold) {
                complete = true;
                approved = true;
            } else if (reject > memberCount - threshold || tribunal.votes.size() == memberCount) {
                complete = true;
                approved = approve >= threshold;
            }
        }

        if (!complete) {
            return AuthorityResponse.pending(AuthorityResponse.WaitingFor.tribunal(tribunal.policy.getMembers()));
        }

        pending.remove(proposalId);
        if (approved) {
            return AuthorityResponse.approved(tribunal.proposal.getIntent().getBody().getScopeProposal());
        }
        return AuthorityResponse.rejected(reasoning != null ? reasoning : "Tribunal rejected");
    }

    public boolean isPending(String proposalId) {
        return pending.containsKey(proposalId);
    }

    public List<AuthorityDecisionEvent> resolveTimeouts(long nowMillis) {
        List<AuthorityDecisionEvent> decisions = new ArrayList<>();
        for (Map.Entry<String, PendingTribunal> entry : List.copyOf(pending.entrySet())) {
            PendingTribunal tribunal = entry.getValue();
            Long timeoutMillis = tribunal.policy.getTimeoutMillis();
            if (timeoutMillis == null || timeoutMillis < 0) {
                continue;
            }
            if (nowMillis - tribunal.enteredAtMillis < timeoutMillis) {
                continue;
            }

            pending.remove(entry.getKey());
            TimeoutAction timeoutAction = tribunal.policy.getOnTimeout();
            if (timeoutAction == TimeoutAction.APPROVE) {
                decisions.add(new AuthorityDecisionEvent(
                        entry.getKey(),
                        AuthorityResponse.approved(tribunal.proposal.getIntent().getBody().getScopeProposal()),
                        "Tribunal timeout approve"
                ));
            } else {
                decisions.add(new AuthorityDecisionEvent(
                        entry.getKey(),
                        AuthorityResponse.rejected("Tribunal timeout reject"),
                        "Tribunal timeout reject"
                ));
            }
        }
        return decisions;
    }

    public void dropPending(Set<String> proposalIds) {
        for (String proposalId : proposalIds) {
            pending.remove(proposalId);
        }
    }
}
