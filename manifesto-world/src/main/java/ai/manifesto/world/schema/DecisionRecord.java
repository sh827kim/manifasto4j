package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: DecisionRecord는 World 스키마 계층에서 decision record 역할을 수행하는 구현 타입입니다.
 * EN: DecisionRecord is an implementation type performing decision record roles in the World schema layer.
 */
public final class DecisionRecord {
    private final DecisionId decisionId;
    private final ProposalId proposalId;
    private final AuthorityRef authority;
    private final FinalDecision decision;
    private final IntentScope approvedScope;
    private final String reasoning;
    private final long decidedAt;

    public DecisionRecord(
            DecisionId decisionId,
            ProposalId proposalId,
            AuthorityRef authority,
            FinalDecision decision,
            IntentScope approvedScope,
            String reasoning,
            long decidedAt
    ) {
        this.decisionId = Objects.requireNonNull(decisionId, "decisionId is required");
        this.proposalId = Objects.requireNonNull(proposalId, "proposalId is required");
        this.authority = Objects.requireNonNull(authority, "authority is required");
        this.decision = Objects.requireNonNull(decision, "decision is required");
        this.approvedScope = approvedScope;
        this.reasoning = reasoning;
        this.decidedAt = decidedAt;
    }

    public DecisionId getDecisionId() { return decisionId; }
    public ProposalId getProposalId() { return proposalId; }
    public AuthorityRef getAuthority() { return authority; }
    public FinalDecision getDecision() { return decision; }
    public IntentScope getApprovedScope() { return approvedScope; }
    public String getReasoning() { return reasoning; }
    public long getDecidedAt() { return decidedAt; }
}
