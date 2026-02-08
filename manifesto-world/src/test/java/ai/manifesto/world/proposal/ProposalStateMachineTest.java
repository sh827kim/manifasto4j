package ai.manifesto.world.proposal;

import ai.manifesto.world.schema.ProposalStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProposalStateMachineTest {

    @Test
    void allowsExpectedTransitions() {
        assertTrue(ProposalStateMachine.isValidTransition(ProposalStatus.SUBMITTED, ProposalStatus.EVALUATING));
        assertTrue(ProposalStateMachine.isValidTransition(ProposalStatus.SUBMITTED, ProposalStatus.REJECTED));
        assertTrue(ProposalStateMachine.isValidTransition(ProposalStatus.EVALUATING, ProposalStatus.APPROVED));
        assertTrue(ProposalStateMachine.isValidTransition(ProposalStatus.EVALUATING, ProposalStatus.REJECTED));
        assertTrue(ProposalStateMachine.isValidTransition(ProposalStatus.APPROVED, ProposalStatus.EXECUTING));
        assertTrue(ProposalStateMachine.isValidTransition(ProposalStatus.EXECUTING, ProposalStatus.COMPLETED));
        assertTrue(ProposalStateMachine.isValidTransition(ProposalStatus.EXECUTING, ProposalStatus.FAILED));
    }

    @Test
    void rejectsInvalidTransitions() {
        assertFalse(ProposalStateMachine.isValidTransition(ProposalStatus.SUBMITTED, ProposalStatus.APPROVED));
        assertFalse(ProposalStateMachine.isValidTransition(ProposalStatus.SUBMITTED, ProposalStatus.EXECUTING));
        assertFalse(ProposalStateMachine.isValidTransition(ProposalStatus.APPROVED, ProposalStatus.EVALUATING));
        assertFalse(ProposalStateMachine.isValidTransition(ProposalStatus.COMPLETED, ProposalStatus.SUBMITTED));
    }

    @Test
    void exposesTransitionMetadata() {
        assertEquals(Set.of(ProposalStatus.EVALUATING, ProposalStatus.REJECTED), ProposalStateMachine.getValidTransitions(ProposalStatus.SUBMITTED));
        assertTrue(ProposalStateMachine.requiresDecision(ProposalStatus.APPROVED));
        assertTrue(ProposalStateMachine.requiresDecision(ProposalStatus.REJECTED));
        assertFalse(ProposalStateMachine.requiresDecision(ProposalStatus.EXECUTING));
        assertTrue(ProposalStateMachine.createsWorld(ProposalStatus.COMPLETED));
        assertTrue(ProposalStateMachine.createsWorld(ProposalStatus.FAILED));
        assertFalse(ProposalStateMachine.createsWorld(ProposalStatus.REJECTED));
    }
}
