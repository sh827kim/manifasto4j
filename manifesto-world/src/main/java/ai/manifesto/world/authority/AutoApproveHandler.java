package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.Proposal;

/**
 * KR: AutoApproveHandler는 특정 도메인 이벤트/요청을 처리하는 핸들러 타입입니다.
 * EN: AutoApproveHandler is a handler type that processes specific domain events or requests.
 */
public final class AutoApproveHandler implements AuthorityHandler {
    @Override
    public AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding) {
        if (binding.getPolicy().getMode().name().equals("AUTO_APPROVE")) {
            return AuthorityResponse.approved(proposal.getIntent().getBody().getScopeProposal());
        }
        throw new IllegalArgumentException("AutoApproveHandler requires AUTO_APPROVE policy");
    }
}
