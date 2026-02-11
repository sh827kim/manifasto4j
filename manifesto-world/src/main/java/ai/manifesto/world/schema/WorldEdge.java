package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: WorldEdge는 World 스키마 계층에서 world edge 역할을 수행하는 구현 타입입니다.
 * EN: WorldEdge is an implementation type performing world edge roles in the World schema layer.
 */
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
