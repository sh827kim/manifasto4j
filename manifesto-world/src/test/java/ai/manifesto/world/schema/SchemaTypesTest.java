package ai.manifesto.world.schema;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaTypesTest {

    @Test
    void keepsActorAndPolicyStructure() {
        ActorRef owner = new ActorRef("owner", ActorKind.HUMAN, "Owner", Set.of("admin"));
        HitlPolicy policy = new HitlPolicy(owner, 60000L, TimeoutAction.REJECT);
        ActorAuthorityBinding binding = new ActorAuthorityBinding(
                new ActorRef("agent-1", ActorKind.AGENT),
                new AuthorityRef("auth-agent-1", AuthorityKind.HUMAN),
                policy
        );

        assertEquals(AuthorityPolicyMode.HITL, binding.getPolicy().getMode());
        assertEquals("owner", policy.getDelegate().getActorId());
    }

    @Test
    void supportsTribunalPolicy() {
        TribunalPolicy policy = new TribunalPolicy(
                List.of(new ActorRef("a1", ActorKind.AGENT), new ActorRef("a2", ActorKind.AGENT)),
                QuorumRule.majority(),
                30000L,
                TimeoutAction.REJECT
        );

        assertEquals(2, policy.getMembers().size());
        assertEquals(QuorumKind.MAJORITY, policy.getQuorum().getKind());
    }

    @Test
    void identifiesTerminalAndIngressStatus() {
        assertTrue(ProposalStatus.COMPLETED.isTerminal());
        assertTrue(ProposalStatus.REJECTED.isTerminal());
        assertTrue(ProposalStatus.SUBMITTED.isIngress());
        assertTrue(ProposalStatus.EVALUATING.isIngress());
    }
}
