package ai.manifesto.world;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Requirement;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.world.events.WorldEvent;
import ai.manifesto.world.events.WorldEventSink;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
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
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.TimeoutAction;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.AuthorityKind;
import ai.manifesto.world.schema.AuthorityRef;
import ai.manifesto.world.types.HostExecutionOptions;
import ai.manifesto.world.types.HostExecutionResult;
import ai.manifesto.world.types.HostExecutor;
import ai.manifesto.world.types.ExecutionKeyPolicy;
import ai.manifesto.world.types.IntentKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManifestoWorldTest {
    private Snapshot genesisSnapshot;

    @BeforeEach
    void setUp() {
        genesisSnapshot = Snapshot.builder()
                .data(Map.of("count", 0))
                .computed(Map.of())
                .system(SystemState.initial())
                .input(Map.of())
                .meta(Snapshot.SnapshotMeta.create(1, 1000L, "seed", "schema-hash"))
                .build();
    }

    @Test
    void autoProposalExecutesAndCreatesWorld() {
        HostExecutor executor = new HostExecutor() {
            @Override
            public HostExecutionResult execute(String executionKey, Snapshot baseSnapshot, Intent intent, HostExecutionOptions options) {
                Snapshot terminal = baseSnapshot.withData(Map.of("count", 1));
                return HostExecutionResult.completed(terminal);
            }
        };

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef actor = new ActorRef("human-1", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());

        IntentInstance intent = createIntentInstance(actor, "increment", "intent-1", "event-1");
        ProposalResult result = world.submitProposal("human-1", intent, genesis.getWorldId(), null);

        assertNotNull(result.getDecision());
        assertNotNull(result.getResultWorld());
        assertEquals(Proposal.class, result.getProposal().getClass());
        assertEquals(2, world.getStore().listWorlds().size());
    }

    @Test
    void hitlProposalWaitsThenExecutes() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("status", "ok")));

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef owner = new ActorRef("owner", ActorKind.HUMAN);
        ActorRef agent = new ActorRef("agent-1", ActorKind.AGENT);
        world.registerActor(agent, new HitlPolicy(owner, null, TimeoutAction.REJECT));

        IntentInstance intent = createIntentInstance(agent, "update", "intent-2", "event-2");
        ProposalResult pending = world.submitProposal("agent-1", intent, genesis.getWorldId(), null);
        assertEquals("PENDING", world.getAuthorityEvaluator().getHitlHandler().isPending(pending.getProposal().getProposalId().value()) ? "PENDING" : "OTHER");

        ProposalResult finalResult = world.processHITLDecision(
                pending.getProposal().getProposalId().value(),
                "approved",
                "ok",
                null
        );

        assertNotNull(finalResult.getDecision());
        assertNotNull(finalResult.getResultWorld());
        assertEquals(2, world.getStore().listWorlds().size());
    }

    @Test
    void emitsEventsAndIncrementsEpochOnBranchSwitch() {
        List<WorldEvent> events = new ArrayList<>();
        WorldEventSink sink = events::add;

        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null, sink);
        World genesis = world.createGenesis(genesisSnapshot);
        world.registerActor(new ActorRef("human-1", ActorKind.HUMAN), new AutoApprovePolicy());

        IntentInstance intent = createIntentInstance(new ActorRef("human-1", ActorKind.HUMAN), "increment", "intent-3", "event-3");
        world.submitProposal("human-1", intent, genesis.getWorldId(), null);

        long before = world.getEpoch();
        world.switchBranch(genesis.getWorldId());
        long after = world.getEpoch();

        assertEquals(before + 1, after);
        assertTrue(events.size() >= 4);
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("world:created")));
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("proposal:submitted")));
        assertTrue(events.stream().allMatch(e -> e.getPayload().containsKey("schemaHash")));
        assertTrue(events.stream().allMatch(e -> e.getPayload().containsKey("epoch")));
    }

    @Test
    void policyEscalationRoutesToTargetAuthority() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef reviewer = new ActorRef("reviewer", ActorKind.HUMAN);
        world.registerActor(reviewer, new AutoApprovePolicy("reviewer auto"));

        ActorRef agent = new ActorRef("agent-esc", ActorKind.AGENT);
        PolicyRulesPolicy policy = new PolicyRulesPolicy(
                List.of(new PolicyRule(
                        PolicyCondition.intentType(Set.of("danger-action")),
                        PolicyRuleDecision.ESCALATE,
                        "needs escalation"
                )),
                PolicyRuleDecision.REJECT,
                new AuthorityRef("auth-reviewer", AuthorityKind.AUTO)
        );
        world.registerActor(agent, policy);

        IntentInstance intent = createIntentInstance(agent, "danger-action", "intent-escalate", "event-escalate");
        ProposalResult result = world.submitProposal("agent-esc", intent, genesis.getWorldId(), null);

        assertEquals(ProposalStatus.COMPLETED, result.getProposal().getStatus());
        assertNotNull(result.getDecision());
        assertEquals("auth-reviewer", result.getDecision().getAuthority().getAuthorityId());
    }

    @Test
    void sameTerminalSnapshotProducesSameWorldIdAndNoDuplicateWorld() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("stable", true)));

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);
        ActorRef actor = new ActorRef("human-dup", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());

        IntentInstance intent1 = createIntentInstance(actor, "same-output", "intent-dup-1", "event-dup-1");
        IntentInstance intent2 = createIntentInstance(actor, "same-output", "intent-dup-2", "event-dup-2");

        ProposalResult result1 = world.submitProposal("human-dup", intent1, genesis.getWorldId(), null);
        ProposalResult result2 = world.submitProposal("human-dup", intent2, genesis.getWorldId(), null);

        assertNotNull(result1.getResultWorld());
        assertNotNull(result2.getResultWorld());
        assertEquals(result1.getResultWorld().getWorldId(), result2.getResultWorld().getWorldId());
        assertEquals(2, world.getStore().listWorlds().size());
    }

    @Test
    void executionKeyUsesAttemptOneAndIsUniquePerProposal() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);
        ActorRef actor = new ActorRef("human-ek", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());

        IntentInstance intent1 = createIntentInstance(actor, "act-1", "intent-ek-1", "event-ek-1");
        IntentInstance intent2 = createIntentInstance(actor, "act-2", "intent-ek-2", "event-ek-2");

        ProposalResult result1 = world.submitProposal("human-ek", intent1, genesis.getWorldId(), null);
        ProposalResult result2 = world.submitProposal("human-ek", intent2, genesis.getWorldId(), null);

        String key1 = result1.getProposal().getExecutionKey();
        String key2 = result2.getProposal().getExecutionKey();

        assertTrue(key1.endsWith(":1"));
        assertTrue(key2.endsWith(":1"));
        assertTrue(!key1.equals(key2));
    }

    @Test
    void queryApisReturnExpectedState() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);
        ActorRef actor = new ActorRef("human-1", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());

        IntentInstance intent = createIntentInstance(actor, "increment", "intent-q1", "event-q1");
        ProposalResult result = world.submitProposal("human-1", intent, genesis.getWorldId(), null);

        assertEquals(1, world.getRegisteredActors().size());
        assertNotNull(world.getProposal(result.getProposal().getProposalId().value()));
        assertNotNull(world.getDecisionByProposal(result.getProposal().getProposalId().value()));
        assertNotNull(world.getWorld(result.getResultWorld().getWorldId()));
        assertNotNull(world.getSnapshot(result.getResultWorld().getWorldId()));
        assertNotNull(world.getGenesis());
        assertEquals(0, world.getEvaluatingProposals().size());
    }

    @Test
    void hitlTimeoutDecisionIsAppliedByTick() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("status", "ok")));

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef owner = new ActorRef("owner", ActorKind.HUMAN);
        ActorRef agent = new ActorRef("agent-timeout", ActorKind.AGENT);
        world.registerActor(agent, new HitlPolicy(owner, 0L, TimeoutAction.REJECT));

        IntentInstance intent = createIntentInstance(agent, "update", "intent-timeout", "event-timeout");
        ProposalResult pending = world.submitProposal("agent-timeout", intent, genesis.getWorldId(), null);
        assertEquals(ProposalStatus.EVALUATING, pending.getProposal().getStatus());

        world.tick(System.currentTimeMillis());

        Proposal updated = world.getProposal(pending.getProposal().getProposalId().value());
        assertNotNull(updated);
        assertEquals(ProposalStatus.REJECTED, updated.getStatus());
        assertNotNull(world.getDecisionByProposal(updated.getProposalId().value()));
    }

    @Test
    void staleHitlProposalIsDroppedOnBranchSwitch() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot);

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef owner = new ActorRef("owner", ActorKind.HUMAN);
        ActorRef agent = new ActorRef("agent-stale", ActorKind.AGENT);
        world.registerActor(agent, new HitlPolicy(owner, null, TimeoutAction.REJECT));

        IntentInstance intent = createIntentInstance(agent, "update", "intent-stale", "event-stale");
        ProposalResult pending = world.submitProposal("agent-stale", intent, genesis.getWorldId(), null);
        String pendingId = pending.getProposal().getProposalId().value();
        assertTrue(world.getAuthorityEvaluator().getHitlHandler().isPending(pendingId));

        world.switchBranch(genesis.getWorldId());

        assertNull(world.getProposal(pendingId));
        assertTrue(!world.getAuthorityEvaluator().getHitlHandler().isPending(pendingId));
    }

    @Test
    void rejectsProposalWhenOriginActorDoesNotMatchSubmitActor() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) -> HostExecutionResult.completed(baseSnapshot);
        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef submitter = new ActorRef("human-origin", ActorKind.HUMAN);
        ActorRef differentOrigin = new ActorRef("other-origin", ActorKind.HUMAN);
        world.registerActor(submitter, new AutoApprovePolicy());

        IntentInstance intent = createIntentInstance(differentOrigin, "increment", "intent-origin-mismatch", "event-origin-mismatch");
        assertThrows(
                IllegalArgumentException.class,
                () -> world.submitProposal(submitter.getActorId(), intent, genesis.getWorldId(), null)
        );
    }

    @Test
    void rejectsProposalWhenIntentKeyDoesNotMatchBody() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) -> HostExecutionResult.completed(baseSnapshot);
        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef actor = new ActorRef("human-key", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());

        IntentBody body = new IntentBody("increment", Map.of("n", 1), null);
        IntentInstance invalid = new IntentInstance(
                body,
                "intent-invalid-key",
                "not-matching-key",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-invalid-key"), actor))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.submitProposal(actor.getActorId(), invalid, genesis.getWorldId(), null)
        );
    }

    @Test
    void rejectsProposalWhenBaseWorldHasPendingRequirements() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) -> HostExecutionResult.completed(baseSnapshot);
        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);

        Snapshot pendingSnapshot = genesisSnapshot.withSystem(
                genesisSnapshot.getSystem().addPendingRequirement(
                        Requirement.builder().id("req-1").type("effect.test").build()
                )
        );
        World genesis = world.createGenesis(pendingSnapshot);

        ActorRef actor = new ActorRef("human-pending", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());
        IntentInstance intent = createIntentInstance(actor, "increment", "intent-pending", "event-pending");

        assertThrows(
                IllegalArgumentException.class,
                () -> world.submitProposal(actor.getActorId(), intent, genesis.getWorldId(), null)
        );
    }

    @Test
    void marksProposalFailedWhenExecutorThrows() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) -> {
            throw new RuntimeException("executor boom");
        };
        List<WorldEvent> events = new ArrayList<>();
        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null, events::add);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef actor = new ActorRef("human-fail", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());
        IntentInstance intent = createIntentInstance(actor, "increment", "intent-fail", "event-fail");

        ProposalResult result = world.submitProposal(actor.getActorId(), intent, genesis.getWorldId(), null);

        assertEquals(ProposalStatus.FAILED, result.getProposal().getStatus());
        assertNotNull(result.getResultWorld());
        assertNotNull(result.getError());
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("execution:failed")));
    }

    @Test
    void supportsCustomExecutionKeyPolicy() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));
        ExecutionKeyPolicy customPolicy = (proposalId, actorId, baseWorld, attempt) ->
                "custom:" + actorId + ":" + baseWorld.value() + ":" + attempt;

        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null, null, customPolicy);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef actor = new ActorRef("human-custom-key", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());
        IntentInstance intent = createIntentInstance(actor, "increment", "intent-custom-key", "event-custom-key");

        ProposalResult result = world.submitProposal(actor.getActorId(), intent, genesis.getWorldId(), null);
        assertTrue(result.getProposal().getExecutionKey().startsWith("custom:human-custom-key:"));
    }

    @Test
    void rejectsSubmissionFromUnregisteredActor() {
        ManifestoWorld world = new ManifestoWorld("schema-hash");
        World genesis = world.createGenesis(genesisSnapshot);
        ActorRef actor = new ActorRef("human-unknown", ActorKind.HUMAN);
        IntentInstance intent = createIntentInstance(actor, "increment", "intent-unreg", "event-unreg");

        assertThrows(
                IllegalArgumentException.class,
                () -> world.submitProposal("not-registered", intent, genesis.getWorldId(), null)
        );
    }

    @Test
    void rejectsSubmissionToNonExistentBaseWorld() {
        ManifestoWorld world = new ManifestoWorld("schema-hash");
        world.createGenesis(genesisSnapshot);
        ActorRef actor = new ActorRef("human-no-base", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());
        IntentInstance intent = createIntentInstance(actor, "increment", "intent-no-base", "event-no-base");

        assertThrows(
                IllegalArgumentException.class,
                () -> world.submitProposal(actor.getActorId(), intent, ai.manifesto.world.schema.WorldId.of("missing-world"), null)
        );
    }

    @Test
    void rejectsCreatingGenesisTwice() {
        ManifestoWorld world = new ManifestoWorld("schema-hash");
        world.createGenesis(genesisSnapshot);
        assertThrows(IllegalStateException.class, () -> world.createGenesis(genesisSnapshot));
    }

    @Test
    void escalationSupportsMultiHopChain() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));
        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef approver = new ActorRef("approver", ActorKind.HUMAN);
        world.registerActor(approver, new AutoApprovePolicy("final approve"));

        ActorRef policyB = new ActorRef("policy-b", ActorKind.AGENT);
        world.registerActor(policyB, new PolicyRulesPolicy(
                List.of(new PolicyRule(
                        PolicyCondition.intentType(Set.of("needs-escalation")),
                        PolicyRuleDecision.ESCALATE,
                        "escalate to approver"
                )),
                PolicyRuleDecision.REJECT,
                new AuthorityRef("auth-approver", AuthorityKind.AUTO)
        ));

        ActorRef policyA = new ActorRef("policy-a", ActorKind.AGENT);
        world.registerActor(policyA, new PolicyRulesPolicy(
                List.of(new PolicyRule(
                        PolicyCondition.intentType(Set.of("needs-escalation")),
                        PolicyRuleDecision.ESCALATE,
                        "escalate to policy-b"
                )),
                PolicyRuleDecision.REJECT,
                new AuthorityRef("auth-policy-b", AuthorityKind.POLICY)
        ));

        IntentInstance intent = createIntentInstance(policyA, "needs-escalation", "intent-hop", "event-hop");
        ProposalResult result = world.submitProposal(policyA.getActorId(), intent, genesis.getWorldId(), null);

        assertEquals(ProposalStatus.COMPLETED, result.getProposal().getStatus());
        assertNotNull(result.getDecision());
        assertEquals("auth-approver", result.getDecision().getAuthority().getAuthorityId());
    }

    @Test
    void escalationFailureFallsBackToRejectedAndEmitsFailureEvent() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
                HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));
        List<WorldEvent> events = new ArrayList<>();
        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null, events::add);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef policyA = new ActorRef("policy-a", ActorKind.AGENT);
        world.registerActor(policyA, new PolicyRulesPolicy(
                List.of(new PolicyRule(
                        PolicyCondition.intentType(Set.of("needs-escalation")),
                        PolicyRuleDecision.ESCALATE,
                        "escalate to missing authority"
                )),
                PolicyRuleDecision.REJECT,
                new AuthorityRef("auth-missing", AuthorityKind.POLICY)
        ));

        IntentInstance intent = createIntentInstance(policyA, "needs-escalation", "intent-esc-fail", "event-esc-fail");
        ProposalResult result = world.submitProposal(policyA.getActorId(), intent, genesis.getWorldId(), null);

        assertEquals(ProposalStatus.REJECTED, result.getProposal().getStatus());
        assertNotNull(result.getDecision());
        assertEquals(ai.manifesto.world.schema.FinalDecisionKind.REJECTED, result.getDecision().getDecision().getKind());
        assertTrue(result.getDecision().getDecision().getReason().contains("no bound actors"));
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("proposal:escalation_failed")));
    }

    @Test
    void tracksLineageDepthForSequentialProposals() {
        final int[] counter = {0};
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) -> {
            counter[0] += 1;
            return HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", counter[0])));
        };
        ManifestoWorld world = new ManifestoWorld("schema-hash", executor, null);
        World genesis = world.createGenesis(genesisSnapshot);

        ActorRef actor = new ActorRef("human-lineage", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());

        ProposalResult r1 = world.submitProposal(actor.getActorId(), createIntentInstance(actor, "a1", "intent-l1", "event-l1"), genesis.getWorldId(), null);
        ProposalResult r2 = world.submitProposal(actor.getActorId(), createIntentInstance(actor, "a2", "intent-l2", "event-l2"), r1.getResultWorld().getWorldId(), null);
        ProposalResult r3 = world.submitProposal(actor.getActorId(), createIntentInstance(actor, "a3", "intent-l3", "event-l3"), r2.getResultWorld().getWorldId(), null);

        assertEquals(3, world.getLineage().getDepth(r3.getResultWorld().getWorldId()));
        assertTrue(world.getLineage().isDescendant(r3.getResultWorld().getWorldId(), genesis.getWorldId()));
        var path = world.getLineage().findPath(genesis.getWorldId(), r3.getResultWorld().getWorldId());
        assertNotNull(path);
        assertEquals(3, path.edges().size());
    }

    private IntentInstance createIntentInstance(ActorRef actor, String type, String intentId, String eventId) {
        IntentBody body = new IntentBody(type, Map.of(), null);
        String intentKey = IntentKeys.computeIntentKey("schema-hash", body);
        return new IntentInstance(
                body,
                intentId,
                intentKey,
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", eventId), actor))
        );
    }
}
