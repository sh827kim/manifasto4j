package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.Proposal;

public final class AutoApproveHandler implements AuthorityHandler {
    @Override
    public AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding) {
        if (binding.getPolicy().getMode().name().equals("AUTO_APPROVE")) {
            return AuthorityResponse.approved(proposal.getIntent().getBody().getScopeProposal());
        }
        throw new IllegalArgumentException("AutoApproveHandler requires AUTO_APPROVE policy");
    }
}
