package ai.manifesto.world;

import ai.manifesto.core.Intent;
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

        IntentInstance intent = new IntentInstance(
                new IntentBody("increment", Map.of(), null),
                "intent-1",
                "intent-key-1",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-1"), actor))
        );

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

        IntentInstance intent = new IntentInstance(
                new IntentBody("update", Map.of(), null),
                "intent-2",
                "intent-key-2",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-2"), agent))
        );

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

        IntentInstance intent = new IntentInstance(
                new IntentBody("increment", Map.of(), null),
                "intent-3",
                "intent-key-3",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-3"), new ActorRef("human-1", ActorKind.HUMAN)))
        );
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

        IntentInstance intent = new IntentInstance(
                new IntentBody("danger-action", Map.of(), null),
                "intent-escalate",
                "intent-key-escalate",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-escalate"), agent))
        );

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

        IntentInstance intent1 = new IntentInstance(
                new IntentBody("same-output", Map.of(), null),
                "intent-dup-1",
                "intent-key-dup-1",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-dup-1"), actor))
        );
        IntentInstance intent2 = new IntentInstance(
                new IntentBody("same-output", Map.of(), null),
                "intent-dup-2",
                "intent-key-dup-2",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-dup-2"), actor))
        );

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

        IntentInstance intent1 = new IntentInstance(
                new IntentBody("act-1", Map.of(), null),
                "intent-ek-1",
                "intent-key-ek-1",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-ek-1"), actor))
        );
        IntentInstance intent2 = new IntentInstance(
                new IntentBody("act-2", Map.of(), null),
                "intent-ek-2",
                "intent-key-ek-2",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-ek-2"), actor))
        );

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

        IntentInstance intent = new IntentInstance(
                new IntentBody("increment", Map.of(), null),
                "intent-q1",
                "intent-key-q1",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-q1"), actor))
        );
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

        IntentInstance intent = new IntentInstance(
                new IntentBody("update", Map.of(), null),
                "intent-timeout",
                "intent-key-timeout",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-timeout"), agent))
        );

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

        IntentInstance intent = new IntentInstance(
                new IntentBody("update", Map.of(), null),
                "intent-stale",
                "intent-key-stale",
                new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-stale"), agent))
        );

        ProposalResult pending = world.submitProposal("agent-stale", intent, genesis.getWorldId(), null);
        String pendingId = pending.getProposal().getProposalId().value();
        assertTrue(world.getAuthorityEvaluator().getHitlHandler().isPending(pendingId));

        world.switchBranch(genesis.getWorldId());

        assertNull(world.getProposal(pendingId));
        assertTrue(!world.getAuthorityEvaluator().getHitlHandler().isPending(pendingId));
    }
}
