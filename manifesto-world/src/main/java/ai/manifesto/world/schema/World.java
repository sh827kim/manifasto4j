package ai.manifesto.world.schema;

import java.util.Objects;

public final class World {
    private final WorldId worldId;
    private final String schemaHash;
    private final String snapshotHash;
    private final long createdAt;
    private final ProposalId createdBy;
    private final ArtifactRef executionTraceRef;

    public World(WorldId worldId, String schemaHash, String snapshotHash, long createdAt, ProposalId createdBy, ArtifactRef executionTraceRef) {
        this.worldId = Objects.requireNonNull(worldId, "worldId is required");
        this.schemaHash = Objects.requireNonNull(schemaHash, "schemaHash is required");
        this.snapshotHash = Objects.requireNonNull(snapshotHash, "snapshotHash is required");
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.executionTraceRef = executionTraceRef;
    }

    public WorldId getWorldId() { return worldId; }
    public String getSchemaHash() { return schemaHash; }
    public String getSnapshotHash() { return snapshotHash; }
    public long getCreatedAt() { return createdAt; }
    public ProposalId getCreatedBy() { return createdBy; }
    public ArtifactRef getExecutionTraceRef() { return executionTraceRef; }
}
