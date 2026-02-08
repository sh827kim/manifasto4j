package ai.manifesto.world.proposal;

import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.DecisionId;
import ai.manifesto.world.schema.IntentBody;
import ai.manifesto.world.schema.IntentInstance;
import ai.manifesto.world.schema.IntentMeta;
import ai.manifesto.world.schema.IntentOrigin;
import ai.manifesto.world.schema.IntentSource;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.WorldId;
import ai.manifesto.world.types.ExecutionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProposalQueueTest {
    private ProposalQueue queue;
    private ActorRef actor;
    private IntentInstance intent;
    private WorldId world1;
    private WorldId world2;

    @BeforeEach
    void setUp() {
        queue = new ProposalQueue();
        actor = new ActorRef("human-1", ActorKind.HUMAN, "Human 1", null);
        intent = new IntentInstance(
                new IntentBody("addTodo", Map.of("title", "test"), null),
                "intent-1",
                "intent-key-1",
                new IntentMeta(new IntentOrigin("test:projection", new IntentSource("ui", "event-1"), actor))
        );
        world1 = WorldId.of("world-1");
        world2 = WorldId.of("world-2");
    }

    @Test
    void submitsProposalWithSubmittedStatus() {
        ProposalId proposalId = ProposalId.of("proposal-1");
        Proposal proposal = queue.submit(
                proposalId,
                ExecutionKeys.createExecutionKey(proposalId, 1),
                actor,
                intent,
                world1,
                null,
                0,
                1000L
        );

        assertEquals(ProposalStatus.SUBMITTED, proposal.getStatus());
        assertEquals(1, queue.size());
        assertTrue(queue.has(proposalId));
    }

    @Test
    void appliesLifecycleTransitions() {
        ProposalId proposalId = ProposalId.of("proposal-1");
        queue.submit(
                proposalId,
                ExecutionKeys.createExecutionKey(proposalId, 1),
                actor,
                intent,
                world1,
                null,
                0,
                1000L
        );

        queue.transition(proposalId, ProposalStatus.EVALUATING, TransitionUpdates.empty());
        queue.transition(
                proposalId,
                ProposalStatus.APPROVED,
                TransitionUpdates.empty()
                        .withDecisionId(DecisionId.of("decision-1"))
                        .withDecidedAt(2000L)
        );
        queue.transition(proposalId, ProposalStatus.EXECUTING, TransitionUpdates.empty());
        Proposal completed = queue.transition(
                proposalId,
                ProposalStatus.COMPLETED,
                TransitionUpdates.empty().withResultWorld(world2).withCompletedAt(3000L)
        );

        assertEquals(ProposalStatus.COMPLETED, completed.getStatus());
        assertEquals(world2, completed.getResultWorld());
        assertEquals(1, queue.getTerminal().size());
        assertEquals(0, queue.getActive().size());
    }

    @Test
    void rejectsApprovedWithoutDecision() {
        ProposalId proposalId = ProposalId.of("proposal-1");
        queue.submit(
                proposalId,
                ExecutionKeys.createExecutionKey(proposalId, 1),
                actor,
                intent,
                world1,
                null,
                0,
                1000L
        );
        queue.transition(proposalId, ProposalStatus.EVALUATING, TransitionUpdates.empty());

        assertThrows(IllegalArgumentException.class,
                () -> queue.transition(proposalId, ProposalStatus.APPROVED, TransitionUpdates.empty()));
    }

    @Test
    void rejectsInvalidTransition() {
        ProposalId proposalId = ProposalId.of("proposal-1");
        queue.submit(
                proposalId,
                ExecutionKeys.createExecutionKey(proposalId, 1),
                actor,
                intent,
                world1,
                null,
                0,
                1000L
        );

        assertThrows(IllegalStateException.class,
                () -> queue.transition(proposalId, ProposalStatus.EXECUTING, TransitionUpdates.empty()));
    }
}
