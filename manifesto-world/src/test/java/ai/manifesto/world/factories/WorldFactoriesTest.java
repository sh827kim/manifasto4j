package ai.manifesto.world.factories;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.world.schema.AuthorityKind;
import ai.manifesto.world.schema.AuthorityRef;
import ai.manifesto.world.schema.DecisionRecord;
import ai.manifesto.world.schema.FinalDecision;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.World;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldFactoriesTest {

    @Test
    void createsGenesisWorld() {
        Snapshot snapshot = snapshot("schema-hash");

        World world = WorldFactories.createGenesisWorld("schema-hash", snapshot, 1234L);

        assertNotNull(world.getWorldId());
        assertEquals("schema-hash", world.getSchemaHash());
        assertEquals(1234L, world.getCreatedAt());
        assertNull(world.getCreatedBy());
    }

    @Test
    void createsWorldFromExecution() {
        Snapshot snapshot = snapshot("schema-hash");
        ProposalId proposalId = ProposalId.of("prop-1");

        World world = WorldFactories.createWorldFromExecution("schema-hash", snapshot, proposalId, 5678L);

        assertNotNull(world.getWorldId());
        assertEquals(proposalId, world.getCreatedBy());
        assertEquals(5678L, world.getCreatedAt());
    }

    @Test
    void validatesSchemaHashConsistency() {
        Snapshot snapshot = snapshot("schema-hash-in-meta");

        assertThrows(IllegalArgumentException.class,
                () -> WorldFactories.createGenesisWorld("different-schema", snapshot, 1L));
    }

    @Test
    void createsDecisionRecord() {
        ProposalId proposalId = ProposalId.of("prop-1");
        AuthorityRef authority = new AuthorityRef("auth-1", AuthorityKind.POLICY);

        DecisionRecord record = WorldFactories.createDecisionRecord(
                proposalId,
                authority,
                FinalDecision.approved(),
                null,
                "ok",
                999L
        );

        assertNotNull(record.getDecisionId());
        assertEquals(proposalId, record.getProposalId());
        assertEquals(authority, record.getAuthority());
        assertEquals(999L, record.getDecidedAt());
    }

    private static Snapshot snapshot(String schemaHash) {
        return Snapshot.builder()
                .data(Map.of("count", 1))
                .computed(Map.of())
                .system(SystemState.initial())
                .input(Map.of())
                .meta(Snapshot.SnapshotMeta.create(1, 1000L, "seed", schemaHash))
                .build();
    }
}
