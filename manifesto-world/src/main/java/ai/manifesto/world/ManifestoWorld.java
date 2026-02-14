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
import ai.manifesto.world.ingress.IngressContext;
import ai.manifesto.world.lineage.WorldLineage;
import ai.manifesto.world.persistence.MemoryWorldStore;
import ai.manifesto.world.persistence.ProposalQuery;
import ai.manifesto.world.persistence.StoreResult;
import ai.manifesto.world.persistence.WorldQuery;
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
import ai.manifesto.world.types.ExecutionKeyPolicy;
import ai.manifesto.world.types.HostExecutionOptions;
import ai.manifesto.world.types.HostExecutionResult;
import ai.manifesto.world.types.HostExecutor;
import ai.manifesto.world.types.IntentKeys;

import java.util.Objects;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * KR: ManifestoWorld는 proposal 제출, authority 평가, world 분기/라인리지 갱신을 오케스트레이션하는 월드 런타임입니다.
 * EN: ManifestoWorld orchestrates proposal submission, authority evaluation, and world branching/lineage updates.
 */
public final class ManifestoWorld {
    private static final String ESCALATE_PREFIX = "ESCALATE:";
    private static final int MAX_ESCALATION_HOPS = 8;

    private final String schemaHash;
    private final WorldStore store;
    private final HostExecutor executor;
    private final WorldEventSink eventSink;
    private final ExecutionKeyPolicy executionKeyPolicy;
    private final IngressContext ingressContext = new IngressContext();
    private WorldId genesisWorldId;
    private WorldId currentHeadWorldId;

    private final ActorRegistry registry = new ActorRegistry();
    private final ProposalQueue proposalQueue = new ProposalQueue();
    private final AuthorityEvaluator authorityEvaluator = new AuthorityEvaluator();
    private final WorldLineage lineage = new WorldLineage();

    public ManifestoWorld(String schemaHash) {
        this(schemaHash, null, null, null, null);
    }

    public ManifestoWorld(String schemaHash, HostExecutor executor, WorldStore store) {
        this(schemaHash, executor, store, null, null);
    }

    public ManifestoWorld(String schemaHash, HostExecutor executor, WorldStore store, WorldEventSink eventSink) {
        this(schemaHash, executor, store, eventSink, null);
    }

    public ManifestoWorld(
            String schemaHash,
            HostExecutor executor,
            WorldStore store,
            WorldEventSink eventSink,
            ExecutionKeyPolicy executionKeyPolicy
    ) {
        this.schemaHash = Objects.requireNonNull(schemaHash, "schemaHash is required");
        this.executor = executor;
        this.store = store != null ? store : new MemoryWorldStore();
        this.eventSink = eventSink != null ? eventSink : new NoopWorldEventSink();
        this.executionKeyPolicy = executionKeyPolicy != null
                ? executionKeyPolicy
                : (proposalId, actorId, baseWorld, attempt) -> ExecutionKeys.createExecutionKey(proposalId, attempt);
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
        this.genesisWorldId = world.getWorldId();
        this.currentHeadWorldId = world.getWorldId();
        emitEvent("world:created", Map.of("worldId", world.getWorldId().value(), "genesis", true));
        return world;
    }

    public void switchBranch(WorldId newBaseWorld) {
        if (!store.hasWorld(newBaseWorld)) {
            throw new IllegalArgumentException("World not found: " + newBaseWorld.value());
        }

        ingressContext.incrementEpoch();
        currentHeadWorldId = newBaseWorld;
        Set<String> staleProposalIds = new HashSet<>();
        for (Proposal proposal : proposalQueue.getIngressStage()) {
            if (ingressContext.isStale(proposal.getEpoch())) {
                proposalQueue.remove(proposal.getProposalId());
                store.deleteProposal(proposal.getProposalId());
                staleProposalIds.add(proposal.getProposalId().value());
                emitEvent(
                        "proposal:superseded",
                        Map.of(
                                "proposalId", proposal.getProposalId().value(),
                                "proposalEpoch", proposal.getEpoch(),
                                "currentEpoch", ingressContext.epoch()
                        )
                );
            }
        }
        authorityEvaluator.dropPending(staleProposalIds);
    }

    public void resume(WorldId worldId) {
        Objects.requireNonNull(worldId, "worldId is required");
        if (!store.hasWorld(worldId)) {
            throw new IllegalArgumentException("World not found: " + worldId.value());
        }
        if (genesisWorldId == null) {
            World genesis = store.getGenesis();
            if (genesis != null) {
                genesisWorldId = genesis.getWorldId();
            }
        }
        currentHeadWorldId = worldId;
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
            if (ingressContext.isStale(proposal.getEpoch())) {
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
        Snapshot baseSnapshot = store.getSnapshot(baseWorld);
        if (baseSnapshot == null) {
            throw new IllegalStateException("Snapshot not found for base world: " + baseWorld.value());
        }
        if (!baseSnapshot.getSystem().getPendingRequirements().isEmpty()) {
            throw new IllegalArgumentException(
                    "Base world has pending requirements and cannot be used as base world: " + baseWorld.value()
            );
        }
        ActorRef originActor = intent.getMeta().getOrigin().getActor();
        if (!binding.getActor().getActorId().equals(originActor.getActorId())
                || binding.getActor().getKind() != originActor.getKind()) {
            throw new IllegalArgumentException("Intent origin actor must match proposal actor");
        }
        String expectedIntentKey = IntentKeys.computeIntentKey(schemaHash, intent.getBody());
        if (!expectedIntentKey.equals(intent.getIntentKey())) {
            throw new IllegalArgumentException("Intent key does not match computed value");
        }

        ProposalId proposalId = ProposalId.of("prop-" + UUID.randomUUID());
        String executionKey = executionKeyPolicy.createExecutionKey(
                proposalId,
                binding.getActor().getActorId(),
                baseWorld,
                1
        );
        if (executionKey == null || executionKey.isBlank()) {
            throw new IllegalStateException("Execution key policy returned empty key");
        }
        long submittedAt = System.currentTimeMillis();
        Proposal proposal = proposalQueue.submit(
                proposalId, executionKey, binding.getActor(), intent, baseWorld, trace, ingressContext.epoch(), submittedAt
        );
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
        if (ingressContext.isStale(proposal.getEpoch())) {
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
        if (ingressContext.isStale(proposal.getEpoch())) {
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

        try {
            HostExecutionResult executionResult = executor.execute(
                    executing.getExecutionKey(),
                    baseSnapshot,
                    hostIntent,
                    new HostExecutionOptions(executing.getApprovedScope())
            );
            if (executionResult == null || executionResult.getTerminalSnapshot() == null) {
                throw new IllegalStateException("Executor returned empty terminal snapshot");
            }

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
            currentHeadWorldId = resultWorld.getWorldId();
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
        } catch (Exception error) {
            long createdAt = baseSnapshot.getMeta() != null ? baseSnapshot.getMeta().getTimestamp() : System.currentTimeMillis();
            World failedWorld = WorldFactories.createWorldFromExecution(schemaHash, baseSnapshot, proposalId, createdAt);
            if (!store.hasWorld(failedWorld.getWorldId())) {
                ensureSuccess(store.saveWorld(failedWorld));
                lineage.addWorldWithEdge(
                        failedWorld,
                        executing.getBaseWorld(),
                        proposalId,
                        decisionRecord.getDecisionId(),
                        System.currentTimeMillis()
                );
                var parentEdge = lineage.getParentEdge(failedWorld.getWorldId());
                if (parentEdge != null) {
                    ensureSuccess(store.saveEdge(parentEdge));
                }
            }
            ensureSuccess(store.saveSnapshot(failedWorld.getWorldId(), baseSnapshot));
            currentHeadWorldId = failedWorld.getWorldId();
            emitEvent(
                    "world:created",
                    Map.of(
                            "worldId", failedWorld.getWorldId().value(),
                            "proposalId", proposalId.value(),
                            "outcome", "failed"
                    )
            );
            emitEvent(
                    "execution:failed",
                    Map.of(
                            "proposalId", proposalId.value(),
                            "worldId", failedWorld.getWorldId().value(),
                            "error", error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName()
                    )
            );

            TransitionUpdates terminalUpdates = TransitionUpdates.empty()
                    .withResultWorld(failedWorld.getWorldId())
                    .withCompletedAt(System.currentTimeMillis());
            Proposal failed = proposalQueue.transition(proposalId, ProposalStatus.FAILED, terminalUpdates);
            ensureSuccess(store.updateProposal(proposalId, terminalUpdates, ProposalStatus.FAILED));
            return new ProposalResult(
                    failed,
                    decisionRecord,
                    failedWorld,
                    error.getMessage() != null ? error.getMessage() : "Execution failed"
            );
        }
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
        int hops = 0;

        while (isEscalationResponse(currentResponse)) {
            hops += 1;
            if (hops > MAX_ESCALATION_HOPS) {
                return escalationFailure(
                        proposal,
                        currentAuthority,
                        "Escalation hop limit exceeded: " + MAX_ESCALATION_HOPS
                );
            }
            String targetAuthorityId = extractEscalationTarget(currentResponse.getReason());
            if (targetAuthorityId == null || targetAuthorityId.isBlank()) {
                return escalationFailure(proposal, currentAuthority, "Invalid escalation target");
            }
            if (visitedAuthorityIds.contains(targetAuthorityId)) {
                return escalationFailure(proposal, currentAuthority, "Escalation loop detected: " + targetAuthorityId);
            }

            List<ActorRef> escalationActors = registry.getActorsByAuthority(targetAuthorityId);
            if (escalationActors.isEmpty()) {
                return escalationFailure(
                        proposal,
                        currentAuthority,
                        "Escalation target authority has no bound actors: " + targetAuthorityId
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
            try {
                currentResponse = authorityEvaluator.evaluate(proposal, escalationBinding);
            } catch (Exception error) {
                String message = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
                return escalationFailure(proposal, currentAuthority, "Escalation evaluation failed: " + message);
            }
        }

        return new EvaluationOutcome(currentAuthority, currentResponse);
    }

    private EvaluationOutcome escalationFailure(
            Proposal proposal,
            AuthorityRef authority,
            String reason
    ) {
        emitEvent(
                "proposal:escalation_failed",
                Map.of(
                        "proposalId", proposal.getProposalId().value(),
                        "authorityId", authority.getAuthorityId(),
                        "reason", reason
                )
        );
        return new EvaluationOutcome(authority, AuthorityResponse.rejected(reason));
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

    public List<World> listWorlds(WorldQuery query) {
        return store.listWorlds(query);
    }

    public List<Proposal> listProposals(ProposalQuery query) {
        return store.listProposals(query);
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

    public WorldId getCurrentHeadWorldId() {
        return currentHeadWorldId;
    }

    public WorldId getGenesisWorldId() {
        return genesisWorldId;
    }

    public boolean isInitialized() {
        return genesisWorldId != null;
    }

    public long getEpoch() {
        return ingressContext.epoch();
    }

    public ActorRef createActor(String actorId, ActorKind kind) {
        return new ActorRef(actorId, kind);
    }

    private void emitEvent(String type, Map<String, Object> payload) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("schemaHash", schemaHash);
        envelope.put("epoch", ingressContext.epoch());
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
