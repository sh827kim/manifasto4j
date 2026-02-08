package ai.manifesto.world;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.world.authority.AuthorityEvaluator;
import ai.manifesto.world.authority.AuthorityDecisionEvent;
import ai.manifesto.world.authority.AuthorityResponse;
import ai.manifesto.world.authority.TribunalHandler;
import ai.manifesto.world.events.NoopWorldEventSink;
import ai.manifesto.world.events.WorldEvent;
import ai.manifesto.world.events.WorldEventSink;
import ai.manifesto.world.factories.WorldFactories;
import ai.manifesto.world.lineage.WorldLineage;
import ai.manifesto.world.persistence.MemoryWorldStore;
import ai.manifesto.world.persistence.StoreResult;
import ai.manifesto.world.persistence.WorldStore;
import ai.manifesto.world.proposal.ProposalQueue;
import ai.manifesto.world.proposal.TransitionUpdates;
import ai.manifesto.world.registry.ActorRegistry;
import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AuthorityKind;
import ai.manifesto.world.schema.AuthorityPolicy;
import ai.manifesto.world.schema.AuthorityPolicyMode;
import ai.manifesto.world.schema.AuthorityRef;
import ai.manifesto.world.schema.DecisionRecord;
import ai.manifesto.world.schema.FinalDecision;
import ai.manifesto.world.schema.IntentInstance;
import ai.manifesto.world.schema.IntentScope;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.ProposalTrace;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.WorldId;
import ai.manifesto.world.types.ExecutionKeys;
import ai.manifesto.world.types.HostExecutionOptions;
import ai.manifesto.world.types.HostExecutionResult;
import ai.manifesto.world.types.HostExecutor;

import java.util.Objects;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ManifestoWorld {
    private static final String ESCALATE_PREFIX = "ESCALATE:";

    private final String schemaHash;
    private final WorldStore store;
    private final HostExecutor executor;
    private final WorldEventSink eventSink;
    private long epoch = 0L;

    private final ActorRegistry registry = new ActorRegistry();
    private final ProposalQueue proposalQueue = new ProposalQueue();
    private final AuthorityEvaluator authorityEvaluator = new AuthorityEvaluator();
    private final WorldLineage lineage = new WorldLineage();

    public ManifestoWorld(String schemaHash) {
        this(schemaHash, null, null, null);
    }

    public ManifestoWorld(String schemaHash, HostExecutor executor, WorldStore store) {
        this(schemaHash, executor, store, null);
    }

    public ManifestoWorld(String schemaHash, HostExecutor executor, WorldStore store, WorldEventSink eventSink) {
        this.schemaHash = Objects.requireNonNull(schemaHash, "schemaHash is required");
        this.executor = executor;
        this.store = store != null ? store : new MemoryWorldStore();
        this.eventSink = eventSink != null ? eventSink : new NoopWorldEventSink();
    }

    public void registerActor(ActorRef actor, AuthorityPolicy policy) {
        AuthorityRef authority = new AuthorityRef("auth-" + actor.getActorId(), toAuthorityKind(policy.getMode()));
        registry.register(actor, authority, policy);
    }

    public void updateActorBinding(String actorId, AuthorityPolicy policy) {
        ActorAuthorityBinding binding = registry.getBindingOrThrow(actorId);
        AuthorityRef authority = new AuthorityRef(binding.getAuthority().getAuthorityId(), toAuthorityKind(policy.getMode()));
        registry.updateBinding(actorId, authority, policy);
    }

    public ActorAuthorityBinding getActorBinding(String actorId) {
        return registry.getBinding(actorId);
    }

    public World createGenesis(Snapshot initialSnapshot) {
        if (store.getGenesis() != null) {
            throw new IllegalStateException("Genesis world already exists");
        }

        long createdAt = initialSnapshot.getMeta() != null ? initialSnapshot.getMeta().getTimestamp() : 0L;
        World world = WorldFactories.createGenesisWorld(schemaHash, initialSnapshot, createdAt);
        ensureSuccess(store.saveWorld(world));
        ensureSuccess(store.saveSnapshot(world.getWorldId(), initialSnapshot));
        ensureSuccess(store.setGenesis(world.getWorldId()));
        lineage.setGenesis(world);
        emitEvent("world:created", Map.of("worldId", world.getWorldId().value(), "genesis", true));
        return world;
    }

    public void switchBranch(WorldId newBaseWorld) {
        if (!store.hasWorld(newBaseWorld)) {
            throw new IllegalArgumentException("World not found: " + newBaseWorld.value());
        }

        epoch += 1;
        Set<String> staleProposalIds = new HashSet<>();
        for (Proposal proposal : proposalQueue.getIngressStage()) {
            if (proposal.getEpoch() < epoch) {
                proposalQueue.remove(proposal.getProposalId());
                store.deleteProposal(proposal.getProposalId());
                staleProposalIds.add(proposal.getProposalId().value());
                emitEvent(
                        "proposal:superseded",
                        Map.of(
                                "proposalId", proposal.getProposalId().value(),
                                "proposalEpoch", proposal.getEpoch(),
                                "currentEpoch", epoch
                        )
                );
            }
        }
        authorityEvaluator.dropPending(staleProposalIds);
    }

    public void tick(long nowMillis) {
        List<AuthorityDecisionEvent> timeoutDecisions = authorityEvaluator.resolveTimeouts(nowMillis);
        for (AuthorityDecisionEvent timeoutDecision : timeoutDecisions) {
            ProposalId proposalId = ProposalId.of(timeoutDecision.getProposalId());
            Proposal proposal = proposalQueue.get(proposalId).orElse(null);
            if (proposal == null) {
                continue;
            }
            if (proposal.getStatus() != ProposalStatus.EVALUATING) {
                continue;
            }
            if (proposal.getEpoch() < epoch) {
                continue;
            }

            ActorAuthorityBinding binding = registry.getBindingOrThrow(proposal.getActor().getActorId());
            applyDecisionAndMaybeExecute(proposal, binding.getAuthority(), timeoutDecision.getResponse(), timeoutDecision.getReason());
        }
    }

    public ProposalResult submitProposal(String actorId, IntentInstance intent, WorldId baseWorld, ProposalTrace trace) {
        ActorAuthorityBinding binding = registry.getBinding(actorId);
        if (binding == null) {
            throw new IllegalArgumentException("Actor not registered: " + actorId);
        }
        if (!store.hasWorld(baseWorld)) {
            throw new IllegalArgumentException("Base world not found: " + baseWorld.value());
        }

        ProposalId proposalId = ProposalId.of("prop-" + UUID.randomUUID());
        String executionKey = ExecutionKeys.createExecutionKey(proposalId, 1);
        long submittedAt = System.currentTimeMillis();
        Proposal proposal = proposalQueue.submit(proposalId, executionKey, binding.getActor(), intent, baseWorld, trace, epoch, submittedAt);
        ensureSuccess(store.saveProposal(proposal));
        emitEvent("proposal:submitted", Map.of("proposalId", proposalId.value(), "epoch", proposal.getEpoch()));

        Proposal evaluating = proposalQueue.transition(proposalId, ProposalStatus.EVALUATING, TransitionUpdates.empty());
        ensureSuccess(store.updateProposal(proposalId, TransitionUpdates.empty(), ProposalStatus.EVALUATING));
        emitEvent("proposal:evaluating", Map.of("proposalId", proposalId.value()));

        AuthorityResponse response = authorityEvaluator.evaluate(evaluating, binding);
        EvaluationOutcome evaluationOutcome = resolveEscalation(evaluating, binding, response);

        if (evaluationOutcome.response().getKind() == AuthorityResponse.Kind.PENDING) {
            return ProposalResult.of(evaluating, null, null);
        }

        return applyDecisionAndMaybeExecute(
                evaluating,
                evaluationOutcome.authority(),
                evaluationOutcome.response(),
                null
        );
    }

    public ProposalResult processHITLDecision(String proposalId, String decision, String reasoning, IntentScope approvedScope) {
        ProposalId pid = ProposalId.of(proposalId);
        Proposal proposal = proposalQueue.getOrThrow(pid);
        if (proposal.getStatus() != ProposalStatus.EVALUATING) {
            throw new IllegalStateException("Proposal is not in evaluating status: " + proposal.getStatus());
        }
        if (proposal.getEpoch() < epoch) {
            return ProposalResult.withError(proposal, "Proposal is stale for current epoch");
        }

        ActorAuthorityBinding binding = registry.getBindingOrThrow(proposal.getActor().getActorId());
        AuthorityResponse response = authorityEvaluator.submitHitlDecision(proposalId, decision, reasoning, approvedScope);
        return applyDecisionAndMaybeExecute(proposal, binding.getAuthority(), response, reasoning);
    }

    public ProposalResult processTribunalVote(String proposalId, ActorRef voter, TribunalHandler.VoteDecision decision, String reasoning) {
        ProposalId pid = ProposalId.of(proposalId);
        Proposal proposal = proposalQueue.getOrThrow(pid);
        if (proposal.getStatus() != ProposalStatus.EVALUATING) {
            throw new IllegalStateException("Proposal is not in evaluating status: " + proposal.getStatus());
        }
        if (proposal.getEpoch() < epoch) {
            return ProposalResult.withError(proposal, "Proposal is stale for current epoch");
        }

        ActorAuthorityBinding binding = registry.getBindingOrThrow(proposal.getActor().getActorId());
        AuthorityResponse response = authorityEvaluator.submitTribunalVote(proposalId, voter, decision, reasoning);
        if (response.getKind() == AuthorityResponse.Kind.PENDING) {
            return ProposalResult.of(proposal, null, null);
        }
        return applyDecisionAndMaybeExecute(proposal, binding.getAuthority(), response, reasoning);
    }

    private ProposalResult applyDecisionAndMaybeExecute(
            Proposal proposal,
            AuthorityRef authority,
            AuthorityResponse response,
            String reasoning
    ) {
        boolean approved = response.getKind() == AuthorityResponse.Kind.APPROVED;
        IntentScope finalApprovedScope = approved ? response.getApprovedScope() : null;

        DecisionRecord decisionRecord = WorldFactories.createDecisionRecord(
                proposal.getProposalId(),
                authority,
                approved ? FinalDecision.approved() : FinalDecision.rejected(response.getReason() != null ? response.getReason() : "Rejected"),
                finalApprovedScope,
                reasoning,
                System.currentTimeMillis()
        );
        ensureSuccess(store.saveDecision(decisionRecord));
        emitEvent(
                "proposal:decided",
                Map.of(
                        "proposalId", proposal.getProposalId().value(),
                        "decisionId", decisionRecord.getDecisionId().value(),
                        "decision", approved ? "approved" : "rejected"
                )
        );

        ProposalStatus status = approved ? ProposalStatus.APPROVED : ProposalStatus.REJECTED;
        TransitionUpdates updates = TransitionUpdates.empty()
                .withDecisionId(decisionRecord.getDecisionId())
                .withDecidedAt(decisionRecord.getDecidedAt())
                .withApprovedScope(finalApprovedScope);

        Proposal updated = proposalQueue.transition(proposal.getProposalId(), status, updates);
        ensureSuccess(store.updateProposal(proposal.getProposalId(), updates, status));

        if (!approved || executor == null) {
            return ProposalResult.of(updated, decisionRecord, null);
        }

        return executeProposal(updated.getProposalId(), decisionRecord);
    }

    private ProposalResult executeProposal(ProposalId proposalId, DecisionRecord decisionRecord) {
        Proposal executing = proposalQueue.transition(proposalId, ProposalStatus.EXECUTING, TransitionUpdates.empty());
        ensureSuccess(store.updateProposal(proposalId, TransitionUpdates.empty(), ProposalStatus.EXECUTING));
        emitEvent("proposal:executing", Map.of("proposalId", proposalId.value(), "executionKey", executing.getExecutionKey()));

        Snapshot baseSnapshot = store.getSnapshot(executing.getBaseWorld());
        if (baseSnapshot == null) {
            throw new IllegalStateException("Snapshot not found for base world: " + executing.getBaseWorld().value());
        }

        Intent hostIntent = new Intent(
                executing.getIntent().getBody().getType(),
                executing.getIntent().getBody().getInput(),
                executing.getIntent().getIntentId()
        );

        HostExecutionResult executionResult = executor.execute(
                executing.getExecutionKey(),
                baseSnapshot,
                hostIntent,
                new HostExecutionOptions(executing.getApprovedScope())
        );

        Snapshot terminalSnapshot = executionResult.getTerminalSnapshot();
        String finalStatus = deriveOutcome(terminalSnapshot);

        long createdAt = terminalSnapshot.getMeta() != null ? terminalSnapshot.getMeta().getTimestamp() : System.currentTimeMillis();
        World resultWorld = WorldFactories.createWorldFromExecution(schemaHash, terminalSnapshot, proposalId, createdAt);
        if (!store.hasWorld(resultWorld.getWorldId())) {
            ensureSuccess(store.saveWorld(resultWorld));
            lineage.addWorldWithEdge(
                    resultWorld,
                    executing.getBaseWorld(),
                    proposalId,
                    decisionRecord.getDecisionId(),
                    System.currentTimeMillis()
            );
            var parentEdge = lineage.getParentEdge(resultWorld.getWorldId());
            if (parentEdge != null) {
                ensureSuccess(store.saveEdge(parentEdge));
            }
        }
        ensureSuccess(store.saveSnapshot(resultWorld.getWorldId(), terminalSnapshot));
        emitEvent(
                "world:created",
                Map.of(
                        "worldId", resultWorld.getWorldId().value(),
                        "proposalId", proposalId.value(),
                        "outcome", finalStatus
                )
        );

        ProposalStatus status = "completed".equals(finalStatus) ? ProposalStatus.COMPLETED : ProposalStatus.FAILED;
        TransitionUpdates terminalUpdates = TransitionUpdates.empty()
                .withResultWorld(resultWorld.getWorldId())
                .withCompletedAt(System.currentTimeMillis());

        Proposal completed = proposalQueue.transition(proposalId, status, terminalUpdates);
        ensureSuccess(store.updateProposal(proposalId, terminalUpdates, status));
        emitEvent("execution:" + finalStatus, Map.of("proposalId", proposalId.value(), "worldId", resultWorld.getWorldId().value()));
        return ProposalResult.of(completed, decisionRecord, resultWorld);
    }

    private EvaluationOutcome resolveEscalation(
            Proposal proposal,
            ActorAuthorityBinding initialBinding,
            AuthorityResponse initialResponse
    ) {
        AuthorityRef currentAuthority = initialBinding.getAuthority();
        AuthorityResponse currentResponse = initialResponse;
        Set<String> visitedAuthorityIds = new HashSet<>();
        visitedAuthorityIds.add(currentAuthority.getAuthorityId());

        while (isEscalationResponse(currentResponse)) {
            String targetAuthorityId = extractEscalationTarget(currentResponse.getReason());
            if (targetAuthorityId == null || targetAuthorityId.isBlank()) {
                return new EvaluationOutcome(
                        currentAuthority,
                        AuthorityResponse.rejected("Invalid escalation target")
                );
            }
            if (visitedAuthorityIds.contains(targetAuthorityId)) {
                return new EvaluationOutcome(
                        currentAuthority,
                        AuthorityResponse.rejected("Escalation loop detected: " + targetAuthorityId)
                );
            }

            List<ActorRef> escalationActors = registry.getActorsByAuthority(targetAuthorityId);
            if (escalationActors.isEmpty()) {
                return new EvaluationOutcome(
                        currentAuthority,
                        AuthorityResponse.rejected("Escalation target authority has no bound actors: " + targetAuthorityId)
                );
            }

            ActorRef escalationActor = escalationActors.get(0);
            ActorAuthorityBinding escalationBinding = registry.getBindingOrThrow(escalationActor.getActorId());
            emitEvent(
                    "proposal:escalated",
                    Map.of(
                            "proposalId", proposal.getProposalId().value(),
                            "fromAuthorityId", currentAuthority.getAuthorityId(),
                            "toAuthorityId", escalationBinding.getAuthority().getAuthorityId(),
                            "viaActorId", escalationActor.getActorId()
                    )
            );

            currentAuthority = escalationBinding.getAuthority();
            visitedAuthorityIds.add(currentAuthority.getAuthorityId());
            currentResponse = authorityEvaluator.evaluate(proposal, escalationBinding);
        }

        return new EvaluationOutcome(currentAuthority, currentResponse);
    }

    private boolean isEscalationResponse(AuthorityResponse response) {
        return response.getKind() == AuthorityResponse.Kind.REJECTED
                && response.getReason() != null
                && response.getReason().startsWith(ESCALATE_PREFIX);
    }

    private String extractEscalationTarget(String reason) {
        if (reason == null || !reason.startsWith(ESCALATE_PREFIX)) {
            return null;
        }
        return reason.substring(ESCALATE_PREFIX.length());
    }

    private String deriveOutcome(Snapshot snapshot) {
        SystemState system = snapshot.getSystem();
        if (system.getLastError() != null) {
            return "failed";
        }
        if (!system.getPendingRequirements().isEmpty()) {
            return "failed";
        }
        return "completed";
    }

    private static <T> void ensureSuccess(StoreResult<T> result) {
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getError() != null ? result.getError() : "Store operation failed");
        }
    }

    private static AuthorityKind toAuthorityKind(AuthorityPolicyMode mode) {
        if (mode == AuthorityPolicyMode.AUTO_APPROVE) {
            return AuthorityKind.AUTO;
        }
        if (mode == AuthorityPolicyMode.HITL) {
            return AuthorityKind.HUMAN;
        }
        if (mode == AuthorityPolicyMode.POLICY_RULES) {
            return AuthorityKind.POLICY;
        }
        return AuthorityKind.TRIBUNAL;
    }

    public WorldStore getStore() {
        return store;
    }

    public AuthorityEvaluator getAuthorityEvaluator() {
        return authorityEvaluator;
    }

    public WorldLineage getLineage() {
        return lineage;
    }

    public ActorRegistry getRegistry() {
        return registry;
    }

    public ProposalQueue getProposalQueue() {
        return proposalQueue;
    }

    public String getSchemaHash() {
        return schemaHash;
    }

    public List<ActorRef> getRegisteredActors() {
        return registry.listActors();
    }

    public Proposal getProposal(String proposalId) {
        return proposalQueue.get(ProposalId.of(proposalId)).orElse(null);
    }

    public List<Proposal> getEvaluatingProposals() {
        return proposalQueue.getEvaluating();
    }

    public World getWorld(WorldId worldId) {
        return store.getWorld(worldId);
    }

    public Snapshot getSnapshot(WorldId worldId) {
        return store.getSnapshot(worldId);
    }

    public DecisionRecord getDecisionByProposal(String proposalId) {
        Proposal proposal = getProposal(proposalId);
        if (proposal == null || proposal.getDecisionId() == null) {
            return null;
        }
        return store.getDecision(proposal.getDecisionId());
    }

    public World getGenesis() {
        return store.getGenesis();
    }

    public long getEpoch() {
        return epoch;
    }

    public ActorRef createActor(String actorId, ActorKind kind) {
        return new ActorRef(actorId, kind);
    }

    private void emitEvent(String type, Map<String, Object> payload) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("schemaHash", schemaHash);
        envelope.put("epoch", epoch);
        if (payload != null) {
            envelope.putAll(payload);
        }
        eventSink.emit(new WorldEvent(type, System.currentTimeMillis(), envelope));
    }

    private record EvaluationOutcome(
            AuthorityRef authority,
            AuthorityResponse response
    ) {}
}
