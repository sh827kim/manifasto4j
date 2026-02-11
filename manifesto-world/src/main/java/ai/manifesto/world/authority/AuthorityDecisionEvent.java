package ai.manifesto.world.authority;

import java.util.Objects;

/**
 * KR: AuthorityDecisionEvent는 World 권한 계층에서 authority decision event 역할을 수행하는 구현 타입입니다.
 * EN: AuthorityDecisionEvent is an implementation type performing authority decision event roles in the World authority layer.
 */
public final class AuthorityDecisionEvent {
    private final String proposalId;
    private final AuthorityResponse response;
    private final String reason;

    public AuthorityDecisionEvent(String proposalId, AuthorityResponse response, String reason) {
        this.proposalId = Objects.requireNonNull(proposalId, "proposalId is required");
        this.response = Objects.requireNonNull(response, "response is required");
        this.reason = reason;
    }

    public String getProposalId() {
        return proposalId;
    }

    public AuthorityResponse getResponse() {
        return response;
    }

    public String getReason() {
        return reason;
    }
}
