package ai.manifesto.world.authority;

import java.util.Objects;

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
