package ai.manifesto.world.schema;

import java.util.Objects;

public final class WorldEdge {
    private final EdgeId edgeId;
    private final WorldId fromWorld;
    private final WorldId toWorld;
    private final ProposalId proposalId;
    private final DecisionId decisionId;
    private final long createdAt;

    public WorldEdge(EdgeId edgeId, WorldId fromWorld, WorldId toWorld, ProposalId proposalId, DecisionId decisionId, long createdAt) {
        this.edgeId = Objects.requireNonNull(edgeId, "edgeId is required");
        this.fromWorld = Objects.requireNonNull(fromWorld, "fromWorld is required");
        this.toWorld = Objects.requireNonNull(toWorld, "toWorld is required");
        this.proposalId = Objects.requireNonNull(proposalId, "proposalId is required");
        this.decisionId = Objects.requireNonNull(decisionId, "decisionId is required");
        this.createdAt = createdAt;
    }

    public EdgeId getEdgeId() { return edgeId; }
    public WorldId getFromWorld() { return fromWorld; }
    public WorldId getToWorld() { return toWorld; }
    public ProposalId getProposalId() { return proposalId; }
    public DecisionId getDecisionId() { return decisionId; }
    public long getCreatedAt() { return createdAt; }
}
