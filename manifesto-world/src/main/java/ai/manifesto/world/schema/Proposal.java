package ai.manifesto.world.schema;

import java.util.Objects;

public final class Proposal {
    private final ProposalId proposalId;
    private final ActorRef actor;
    private final IntentInstance intent;
    private final WorldId baseWorld;
    private final ProposalStatus status;
    private final long epoch;
    private final String executionKey;
    private final ProposalTrace trace;
    private final long submittedAt;
    private final IntentScope approvedScope;
    private final DecisionId decisionId;
    private final WorldId resultWorld;
    private final Long decidedAt;
    private final Long completedAt;

    public Proposal(
            ProposalId proposalId,
            ActorRef actor,
            IntentInstance intent,
            WorldId baseWorld,
            ProposalStatus status,
            long epoch,
            String executionKey,
            ProposalTrace trace,
            long submittedAt,
            IntentScope approvedScope,
            DecisionId decisionId,
            WorldId resultWorld,
            Long decidedAt,
            Long completedAt
    ) {
        this.proposalId = Objects.requireNonNull(proposalId, "proposalId is required");
        this.actor = Objects.requireNonNull(actor, "actor is required");
        this.intent = Objects.requireNonNull(intent, "intent is required");
        this.baseWorld = Objects.requireNonNull(baseWorld, "baseWorld is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.epoch = epoch;
        this.executionKey = Objects.requireNonNull(executionKey, "executionKey is required");
        this.trace = trace;
        this.submittedAt = submittedAt;
        this.approvedScope = approvedScope;
        this.decisionId = decisionId;
        this.resultWorld = resultWorld;
        this.decidedAt = decidedAt;
        this.completedAt = completedAt;
    }

    public static Proposal submitted(
            ProposalId proposalId,
            String executionKey,
            ActorRef actor,
            IntentInstance intent,
            WorldId baseWorld,
            ProposalTrace trace,
            long epoch,
            long submittedAt
    ) {
        return new Proposal(
                proposalId,
                actor,
                intent,
                baseWorld,
                ProposalStatus.SUBMITTED,
                epoch,
                executionKey,
                trace,
                submittedAt,
                null,
                null,
                null,
                null,
                null
        );
    }

    public Proposal withTransition(
            ProposalStatus nextStatus,
            IntentScope nextApprovedScope,
            DecisionId nextDecisionId,
            WorldId nextResultWorld,
            Long nextDecidedAt,
            Long nextCompletedAt
    ) {
        return new Proposal(
                proposalId,
                actor,
                intent,
                baseWorld,
                nextStatus,
                epoch,
                executionKey,
                trace,
                submittedAt,
                nextApprovedScope != null ? nextApprovedScope : approvedScope,
                nextDecisionId != null ? nextDecisionId : decisionId,
                nextResultWorld != null ? nextResultWorld : resultWorld,
                nextDecidedAt != null ? nextDecidedAt : decidedAt,
                nextCompletedAt != null ? nextCompletedAt : completedAt
        );
    }

    public ProposalId getProposalId() { return proposalId; }
    public ActorRef getActor() { return actor; }
    public IntentInstance getIntent() { return intent; }
    public WorldId getBaseWorld() { return baseWorld; }
    public ProposalStatus getStatus() { return status; }
    public long getEpoch() { return epoch; }
    public String getExecutionKey() { return executionKey; }
    public ProposalTrace getTrace() { return trace; }
    public long getSubmittedAt() { return submittedAt; }
    public IntentScope getApprovedScope() { return approvedScope; }
    public DecisionId getDecisionId() { return decisionId; }
    public WorldId getResultWorld() { return resultWorld; }
    public Long getDecidedAt() { return decidedAt; }
    public Long getCompletedAt() { return completedAt; }
}
