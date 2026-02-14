package ai.manifesto.world.persistence;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.world.proposal.TransitionUpdates;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
import ai.manifesto.world.schema.DecisionId;
import ai.manifesto.world.schema.DecisionRecord;
import ai.manifesto.world.schema.EdgeId;
import ai.manifesto.world.schema.FinalDecision;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.WorldEdge;
import ai.manifesto.world.schema.WorldId;
import ai.manifesto.world.types.ExecutionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryWorldStoreTest {
    private MemoryWorldStore store;

    @BeforeEach
    void setUp() {
        store = new MemoryWorldStore();
    }

    @Test
    void storesWorldSnapshotAndEdge() {
        World w1 = new World(WorldId.of("w1"), "schema", "s1", 1L, null, null);
        World w2 = new World(WorldId.of("w2"), "schema", "s2", 2L, ProposalId.of("p1"), null);
        assertTrue(store.saveWorld(w1).isSuccess());
        assertTrue(store.saveWorld(w2).isSuccess());

        Snapshot snapshot = Snapshot.builder()
                .data(Map.of("x", 1))
                .computed(Map.of())
                .system(SystemState.initial())
                .input(Map.of())
                .meta(Snapshot.SnapshotMeta.create(1, 1L, "seed", "schema"))
                .build();

        assertTrue(store.saveSnapshot(w1.getWorldId(), snapshot).isSuccess());
        assertEquals(snapshot, store.getSnapshot(w1.getWorldId()));

        WorldEdge edge = new WorldEdge(
                EdgeId.of("e1"),
                w1.getWorldId(),
                w2.getWorldId(),
                ProposalId.of("p1"),
                DecisionId.of("d1"),
                3L
        );
        assertTrue(store.saveEdge(edge).isSuccess());
        assertEquals(1, store.listEdges().size());
    }

    @Test
    void storesProposalDecisionAndBinding() {
        ProposalId proposalId = ProposalId.of("p1");
        Proposal proposal = Proposal.submitted(
                proposalId,
                ExecutionKeys.createExecutionKey(proposalId, 1),
                new ActorRef("alice", ActorKind.HUMAN),
                new ai.manifesto.world.schema.IntentInstance(
                        new ai.manifesto.world.schema.IntentBody("act", Map.of(), null),
                        "intent-1",
                        "intent-key-1",
                        new ai.manifesto.world.schema.IntentMeta(
                                new ai.manifesto.world.schema.IntentOrigin(
                                        "projection",
                                        new ai.manifesto.world.schema.IntentSource("ui", "event-1"),
                                        new ActorRef("alice", ActorKind.HUMAN)
                                )
                        )
                ),
                WorldId.of("w1"),
                null,
                0,
                10L
        );

        assertTrue(store.saveProposal(proposal).isSuccess());
        assertEquals(1, store.listProposals().size());

        StoreResult<Proposal> updated = store.updateProposal(
                proposalId,
                TransitionUpdates.empty().withDecisionId(DecisionId.of("d1")).withDecidedAt(20L),
                ProposalStatus.APPROVED
        );
        assertTrue(updated.isSuccess());
        assertEquals(ProposalStatus.APPROVED, updated.getData().getStatus());

        DecisionRecord decision = new DecisionRecord(
                DecisionId.of("d1"),
                proposalId,
                new ai.manifesto.world.schema.AuthorityRef("auth", ai.manifesto.world.schema.AuthorityKind.AUTO),
                FinalDecision.approved(),
                null,
                null,
                20L
        );
        assertTrue(store.saveDecision(decision).isSuccess());
        assertEquals(decision, store.getDecisionByProposal(proposalId));

        assertTrue(store.saveBinding(new ai.manifesto.world.schema.ActorAuthorityBinding(
                new ActorRef("alice", ActorKind.HUMAN),
                new ai.manifesto.world.schema.AuthorityRef("auth", ai.manifesto.world.schema.AuthorityKind.AUTO),
                new AutoApprovePolicy()
        )).isSuccess());
        assertEquals(1, store.listBindings().size());
    }


    @Test
    void listProposalsByStatusFiltersExpectedItems() {
        ProposalId approvedId = ProposalId.of("p-approved");
        ProposalId evaluatingId = ProposalId.of("p-evaluating");

        Proposal approved = Proposal.submitted(
                approvedId,
                ExecutionKeys.createExecutionKey(approvedId, 1),
                new ActorRef("alice", ActorKind.HUMAN),
                new ai.manifesto.world.schema.IntentInstance(
                        new ai.manifesto.world.schema.IntentBody("act", Map.of(), null),
                        "intent-approved",
                        "intent-key-approved",
                        new ai.manifesto.world.schema.IntentMeta(
                                new ai.manifesto.world.schema.IntentOrigin(
                                        "projection",
                                        new ai.manifesto.world.schema.IntentSource("ui", "event-approved"),
                                        new ActorRef("alice", ActorKind.HUMAN)
                                )
                        )
                ),
                WorldId.of("w1"),
                null,
                0,
                10L
        ).withTransition(ProposalStatus.APPROVED, null, null, null, null, null);

        Proposal evaluating = Proposal.submitted(
                evaluatingId,
                ExecutionKeys.createExecutionKey(evaluatingId, 1),
                new ActorRef("bob", ActorKind.HUMAN),
                new ai.manifesto.world.schema.IntentInstance(
                        new ai.manifesto.world.schema.IntentBody("act", Map.of(), null),
                        "intent-evaluating",
                        "intent-key-evaluating",
                        new ai.manifesto.world.schema.IntentMeta(
                                new ai.manifesto.world.schema.IntentOrigin(
                                        "projection",
                                        new ai.manifesto.world.schema.IntentSource("ui", "event-evaluating"),
                                        new ActorRef("bob", ActorKind.HUMAN)
                                )
                        )
                ),
                WorldId.of("w2"),
                null,
                0,
                10L
        ).withTransition(ProposalStatus.EVALUATING, null, null, null, null, null);

        assertTrue(store.saveProposal(approved).isSuccess());
        assertTrue(store.saveProposal(evaluating).isSuccess());

        assertEquals(1, store.listProposalsByStatus(ProposalStatus.APPROVED).size());
        assertEquals(1, store.listProposalsByStatus(ProposalStatus.EVALUATING).size());
        assertEquals(0, store.listProposalsByStatus(ProposalStatus.FAILED).size());
    }
    @Test
    void saveSnapshotStripsPlatformNamespacesAndReturnsDefensiveCopy() {
        World world = new World(WorldId.of("w-platform"), "schema", "s-platform", 1L, null, null);
        assertTrue(store.saveWorld(world).isSuccess());

        Snapshot snapshot = Snapshot.builder()
            .data(Map.of(
                "count", 1,
                "$host", Map.of("currentIntentId", "intent-1"),
                "$mel", Map.of("guards", Map.of("intent", Map.of("g1", "intent-1")))
            ))
            .computed(Map.of())
            .system(SystemState.initial())
            .input(Map.of())
            .meta(Snapshot.SnapshotMeta.create(1, 1L, "seed", "schema"))
            .build();

        assertTrue(store.saveSnapshot(world.getWorldId(), snapshot).isSuccess());
        Snapshot loaded1 = store.getSnapshot(world.getWorldId());
        assertNotNull(loaded1);
        assertEquals(1, loaded1.getData().get("count"));
        assertFalse(loaded1.getData().containsKey("$host"));
        assertFalse(loaded1.getData().containsKey("$mel"));

        var mutatedData = loaded1.getData();
        mutatedData.put("count", 999);

        Snapshot loaded2 = store.getSnapshot(world.getWorldId());
        assertEquals(1, loaded2.getData().get("count"));
    }

    @Test
    void listQueriesApplyFilterSortAndLimitContracts() {
        World w1 = new World(WorldId.of("w1"), "schema-a", "s1", 1L, null, null);
        World w2 = new World(WorldId.of("w2"), "schema-b", "s2", 2L, null, null);
        World w3 = new World(WorldId.of("w3"), "schema-a", "s3", 3L, null, null);
        assertTrue(store.saveWorld(w1).isSuccess());
        assertTrue(store.saveWorld(w2).isSuccess());
        assertTrue(store.saveWorld(w3).isSuccess());

        Proposal p1 = Proposal.submitted(
            ProposalId.of("p1"),
            ExecutionKeys.createExecutionKey(ProposalId.of("p1"), 1),
            new ActorRef("alice", ActorKind.HUMAN),
            new ai.manifesto.world.schema.IntentInstance(
                new ai.manifesto.world.schema.IntentBody("act", Map.of(), null),
                "intent-1",
                "intent-key-1",
                new ai.manifesto.world.schema.IntentMeta(
                    new ai.manifesto.world.schema.IntentOrigin(
                        "projection",
                        new ai.manifesto.world.schema.IntentSource("ui", "event-1"),
                        new ActorRef("alice", ActorKind.HUMAN)
                    )
                )
            ),
            WorldId.of("w1"),
            null,
            0,
            10L
        ).withTransition(ProposalStatus.EVALUATING, null, null, null, null, null);
        Proposal p2 = Proposal.submitted(
            ProposalId.of("p2"),
            ExecutionKeys.createExecutionKey(ProposalId.of("p2"), 1),
            new ActorRef("bob", ActorKind.HUMAN),
            p1.getIntent(),
            WorldId.of("w2"),
            null,
            0,
            20L
        ).withTransition(ProposalStatus.APPROVED, null, null, null, null, null);
        assertTrue(store.saveProposal(p1).isSuccess());
        assertTrue(store.saveProposal(p2).isSuccess());

        WorldQuery worldQuery = new WorldQuery("schema-a", null, null, true, 0, 1);
        assertEquals(1, store.listWorlds(worldQuery).size());
        assertEquals("w3", store.listWorlds(worldQuery).get(0).getWorldId().value());

        ProposalQuery proposalQuery = new ProposalQuery(
            Set.of(ProposalStatus.EVALUATING),
            "alice",
            "w1",
            null,
            null,
            false,
            0,
            10
        );
        assertEquals(1, store.listProposals(proposalQuery).size());
        assertEquals("p1", store.listProposals(proposalQuery).get(0).getProposalId().value());
    }
}
