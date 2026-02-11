package ai.manifesto.world.factories;

import ai.manifesto.core.Snapshot;
import ai.manifesto.world.schema.AuthorityRef;
import ai.manifesto.world.schema.DecisionId;
import ai.manifesto.world.schema.DecisionRecord;
import ai.manifesto.world.schema.FinalDecision;
import ai.manifesto.world.schema.IntentScope;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.WorldId;

import java.util.Objects;
import java.util.UUID;

/**
 * KR: WorldFactories는 도메인 객체 생성을 일관되게 구성하는 팩토리 타입입니다.
 * EN: WorldFactories is a factory type that constructs domain objects consistently.
 */
public final class WorldFactories {
    private WorldFactories() {
    }

    public static World createGenesisWorld(String schemaHash, Snapshot snapshot, long createdAt) {
        validateSchemaHashConsistency(schemaHash, snapshot);

        String snapshotHash = WorldHashing.computeSnapshotHash(snapshot);
        WorldId worldId = WorldHashing.computeWorldId(schemaHash, snapshotHash);
        return new World(worldId, schemaHash, snapshotHash, createdAt, null, null);
    }

    public static World createWorldFromExecution(
            String schemaHash,
            Snapshot snapshot,
            ProposalId proposalId,
            long createdAt
    ) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        validateSchemaHashConsistency(schemaHash, snapshot);

        String snapshotHash = WorldHashing.computeSnapshotHash(snapshot);
        WorldId worldId = WorldHashing.computeWorldId(schemaHash, snapshotHash);
        return new World(worldId, schemaHash, snapshotHash, createdAt, proposalId, null);
    }

    public static DecisionRecord createDecisionRecord(
            ProposalId proposalId,
            AuthorityRef authority,
            FinalDecision decision,
            IntentScope approvedScope,
            String reasoning,
            long decidedAt
    ) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        Objects.requireNonNull(authority, "authority is required");
        Objects.requireNonNull(decision, "decision is required");

        DecisionId decisionId = DecisionId.of("dec-" + UUID.randomUUID());
        return new DecisionRecord(
                decisionId,
                proposalId,
                authority,
                decision,
                approvedScope,
                reasoning,
                decidedAt
        );
    }

    private static void validateSchemaHashConsistency(String schemaHash, Snapshot snapshot) {
        Objects.requireNonNull(schemaHash, "schemaHash is required");
        Objects.requireNonNull(snapshot, "snapshot is required");

        String snapshotSchemaHash = snapshot.getMeta() != null ? snapshot.getMeta().getSchemaHash() : null;
        if (snapshotSchemaHash != null && !snapshotSchemaHash.isBlank() && !schemaHash.equals(snapshotSchemaHash)) {
            throw new IllegalArgumentException(
                    "WORLD-SCHEMA-1 violation: provided schemaHash (" + schemaHash + ") does not match snapshot.meta.schemaHash (" + snapshotSchemaHash + ")"
            );
        }
    }
}
