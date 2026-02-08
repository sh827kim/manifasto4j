package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
import ai.manifesto.world.schema.AuthorityKind;
import ai.manifesto.world.schema.AuthorityRef;
import ai.manifesto.world.schema.HitlPolicy;
import ai.manifesto.world.schema.IntentBody;
import ai.manifesto.world.schema.IntentInstance;
import ai.manifesto.world.schema.IntentMeta;
import ai.manifesto.world.schema.IntentOrigin;
import ai.manifesto.world.schema.IntentSource;
import ai.manifesto.world.schema.PolicyCondition;
import ai.manifesto.world.schema.PolicyRule;
import ai.manifesto.world.schema.PolicyRuleDecision;
import ai.manifesto.world.schema.PolicyRulesPolicy;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.TribunalPolicy;
import ai.manifesto.world.schema.QuorumRule;
import ai.manifesto.world.schema.TimeoutAction;
import ai.manifesto.world.schema.WorldId;
import ai.manifesto.world.types.ExecutionKeys;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorityEvaluatorTest {

    @Test
    void evaluatesAutoPolicy() {
        AuthorityEvaluator evaluator = new AuthorityEvaluator();
        ActorRef actor = new ActorRef("human-1", ActorKind.HUMAN);
        Proposal proposal = proposal(actor, "read");
        ActorAuthorityBinding binding = new ActorAuthorityBinding(
                actor,
                new AuthorityRef("auth-human-1", AuthorityKind.AUTO),
                new AutoApprovePolicy()
        );

        AuthorityResponse response = evaluator.evaluate(proposal, binding);

        assertEquals(AuthorityResponse.Kind.APPROVED, response.getKind());
    }

    @Test
    void evaluatesPolicyRules() {
        AuthorityEvaluator evaluator = new AuthorityEvaluator();
        ActorRef actor = new ActorRef("agent-1", ActorKind.AGENT);
        Proposal proposal = proposal(actor, "admin:delete");

        PolicyRulesPolicy policy = new PolicyRulesPolicy(
                List.of(new PolicyRule(
                        PolicyCondition.scopePattern("admin:*"),
                        PolicyRuleDecision.REJECT,
                        "blocked"
                )),
                PolicyRuleDecision.APPROVE,
                null
        );

        ActorAuthorityBinding binding = new ActorAuthorityBinding(
                actor,
                new AuthorityRef("auth-agent-1", AuthorityKind.POLICY),
                policy
        );

        AuthorityResponse response = evaluator.evaluate(proposal, binding);
        assertEquals(AuthorityResponse.Kind.REJECTED, response.getKind());
        assertEquals("blocked", response.getReason());
    }

    @Test
    void handlesHitlAndTribunalPending() {
        AuthorityEvaluator evaluator = new AuthorityEvaluator();
        ActorRef actor = new ActorRef("agent-1", ActorKind.AGENT);

        Proposal hitlProposal = proposal(actor, "write");
        ActorAuthorityBinding hitlBinding = new ActorAuthorityBinding(
                actor,
                new AuthorityRef("auth-hitl", AuthorityKind.HUMAN),
                new HitlPolicy(new ActorRef("owner", ActorKind.HUMAN), 1000L, TimeoutAction.REJECT)
        );

        AuthorityResponse hitlResponse = evaluator.evaluate(hitlProposal, hitlBinding);
        assertEquals(AuthorityResponse.Kind.PENDING, hitlResponse.getKind());

        AuthorityResponse hitlApproved = evaluator.submitHitlDecision(hitlProposal.getProposalId().value(), "approved", null, null);
        assertEquals(AuthorityResponse.Kind.APPROVED, hitlApproved.getKind());

        Proposal tribunalProposal = proposal(actor, "review");
        ActorRef t1 = new ActorRef("t1", ActorKind.AGENT);
        ActorRef t2 = new ActorRef("t2", ActorKind.AGENT);
        ActorRef t3 = new ActorRef("t3", ActorKind.AGENT);

        ActorAuthorityBinding tribunalBinding = new ActorAuthorityBinding(
                actor,
                new AuthorityRef("auth-tribunal", AuthorityKind.TRIBUNAL),
                new TribunalPolicy(List.of(t1, t2, t3), QuorumRule.majority(), null, null)
        );

        AuthorityResponse tribunalResponse = evaluator.evaluate(tribunalProposal, tribunalBinding);
        assertEquals(AuthorityResponse.Kind.PENDING, tribunalResponse.getKind());

        AuthorityResponse stillPending = evaluator.submitTribunalVote(tribunalProposal.getProposalId().value(), t1, TribunalHandler.VoteDecision.APPROVE, null);
        assertEquals(AuthorityResponse.Kind.PENDING, stillPending.getKind());

        AuthorityResponse approved = evaluator.submitTribunalVote(tribunalProposal.getProposalId().value(), t2, TribunalHandler.VoteDecision.APPROVE, null);
        assertEquals(AuthorityResponse.Kind.APPROVED, approved.getKind());
        assertTrue(approved.getApprovedScope() == null);
    }

    private static Proposal proposal(ActorRef actor, String type) {
        ProposalId proposalId = ProposalId.of("prop-" + type.replace(':', '-'));
        IntentInstance intent = new IntentInstance(
                new IntentBody(type, Map.of(), null),
                "intent-" + type,
                "intent-key-" + type,
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-1"), actor))
        );

        return Proposal.submitted(
                proposalId,
                ExecutionKeys.createExecutionKey(proposalId, 1),
                actor,
                intent,
                WorldId.of("world-1"),
                null,
                0,
                1L
        );
    }
}
