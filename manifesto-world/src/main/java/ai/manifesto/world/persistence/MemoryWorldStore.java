package ai.manifesto.world.persistence;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.utils.SnapshotStoreUtils;
import ai.manifesto.world.proposal.TransitionUpdates;
import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.DecisionId;
import ai.manifesto.world.schema.DecisionRecord;
import ai.manifesto.world.schema.EdgeId;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.WorldEdge;
import ai.manifesto.world.schema.WorldId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * KR: MemoryWorldStore는 world/snapshot/proposal/decision 데이터를 메모리에 저장하는 WorldStore 구현입니다.
 * EN: MemoryWorldStore is a WorldStore implementation that persists world, snapshot, proposal, and decision data in memory.
 */
public final class MemoryWorldStore implements ObservableWorldStore {
    private final Map<String, World> worlds = new LinkedHashMap<>();
    private final Map<String, Snapshot> snapshots = new HashMap<>();
    private final Map<String, WorldEdge> edges = new LinkedHashMap<>();
    private final Map<String, Proposal> proposals = new LinkedHashMap<>();
    private final Map<String, DecisionRecord> decisions = new LinkedHashMap<>();
    private final Map<String, String> decisionByProposal = new HashMap<>();
    private final Map<String, ActorAuthorityBinding> bindings = new LinkedHashMap<>();

    private WorldId genesisId;
    private final Map<StoreEventType, Set<StoreEventListener>> typedListeners = new HashMap<>();
    private final Set<StoreEventListener> globalListeners = new CopyOnWriteArraySet<>();

    @Override
    public StoreResult<World> saveWorld(World world) {
        if (worlds.containsKey(world.getWorldId().value())) {
            return StoreResult.failure(
                WorldErrorCode.WORLD_ALREADY_EXISTS,
                "World already exists: " + world.getWorldId().value()
            );
        }
        worlds.put(world.getWorldId().value(), world);
        emit(StoreEventType.WORLD_SAVED, world);
        return StoreResult.success(world);
    }

    @Override
    public World getWorld(WorldId worldId) {
        return worlds.get(worldId.value());
    }

    @Override
    public boolean hasWorld(WorldId worldId) {
        return worlds.containsKey(worldId.value());
    }

    @Override
    public List<World> listWorlds() {
        return new ArrayList<>(worlds.values());
    }

    @Override
    public World getGenesis() {
        return genesisId == null ? null : worlds.get(genesisId.value());
    }

    @Override
    public StoreResult<Void> setGenesis(WorldId worldId) {
        if (genesisId != null) {
            return StoreResult.failure(WorldErrorCode.GENESIS_ALREADY_SET, "Genesis already set");
        }
        if (!worlds.containsKey(worldId.value())) {
            return StoreResult.failure(WorldErrorCode.WORLD_NOT_FOUND, "World does not exist: " + worldId.value());
        }
        genesisId = worldId;
        emit(StoreEventType.GENESIS_SET, worldId);
        return StoreResult.success();
    }

    @Override
    public StoreResult<Void> saveSnapshot(WorldId worldId, Snapshot snapshot) {
        if (!worlds.containsKey(worldId.value())) {
            return StoreResult.failure(WorldErrorCode.WORLD_NOT_FOUND, "World does not exist: " + worldId.value());
        }
        snapshots.put(worldId.value(), SnapshotStoreUtils.canonicalizeForStorage(snapshot));
        return StoreResult.success();
    }

    @Override
    public Snapshot getSnapshot(WorldId worldId) {
        return SnapshotStoreUtils.deepCopySnapshot(snapshots.get(worldId.value()));
    }

    @Override
    public StoreResult<WorldEdge> saveEdge(WorldEdge edge) {
        String edgeId = edge.getEdgeId().value();
        if (edges.containsKey(edgeId)) {
            return StoreResult.failure(WorldErrorCode.EDGE_ALREADY_EXISTS, "Edge already exists: " + edgeId);
        }
        if (!worlds.containsKey(edge.getFromWorld().value())) {
            return StoreResult.failure(
                WorldErrorCode.EDGE_SOURCE_WORLD_NOT_FOUND,
                "Source world does not exist: " + edge.getFromWorld().value()
            );
        }
        if (!worlds.containsKey(edge.getToWorld().value())) {
            return StoreResult.failure(
                WorldErrorCode.EDGE_TARGET_WORLD_NOT_FOUND,
                "Target world does not exist: " + edge.getToWorld().value()
            );
        }
        edges.put(edgeId, edge);
        emit(StoreEventType.EDGE_SAVED, edge);
        return StoreResult.success(edge);
    }

    @Override
    public WorldEdge getEdge(EdgeId edgeId) {
        return edges.get(edgeId.value());
    }

    @Override
    public WorldEdge getParentEdge(WorldId worldId) {
        for (WorldEdge edge : edges.values()) {
            if (edge.getToWorld().equals(worldId)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public List<WorldEdge> getChildEdges(WorldId worldId) {
        List<WorldEdge> results = new ArrayList<>();
        for (WorldEdge edge : edges.values()) {
            if (edge.getFromWorld().equals(worldId)) {
                results.add(edge);
            }
        }
        return results;
    }

    @Override
    public List<WorldEdge> listEdges() {
        return new ArrayList<>(edges.values());
    }

    @Override
    public StoreResult<Proposal> saveProposal(Proposal proposal) {
        if (proposals.containsKey(proposal.getProposalId().value())) {
            return StoreResult.failure(
                WorldErrorCode.PROPOSAL_ALREADY_EXISTS,
                "Proposal already exists: " + proposal.getProposalId().value()
            );
        }
        proposals.put(proposal.getProposalId().value(), proposal);
        emit(StoreEventType.PROPOSAL_SAVED, proposal);
        return StoreResult.success(proposal);
    }

    @Override
    public StoreResult<Proposal> updateProposal(ProposalId proposalId, TransitionUpdates updates, ProposalStatus nextStatus) {
        Proposal proposal = proposals.get(proposalId.value());
        if (proposal == null) {
            return StoreResult.failure(WorldErrorCode.PROPOSAL_NOT_FOUND, "Proposal not found: " + proposalId.value());
        }

        TransitionUpdates resolved = updates != null ? updates : TransitionUpdates.empty();
        Proposal updated = proposal.withTransition(
                nextStatus,
                resolved.getApprovedScope(),
                resolved.getDecisionId(),
                resolved.getResultWorld(),
                resolved.getDecidedAt(),
                resolved.getCompletedAt()
        );
        proposals.put(proposalId.value(), updated);
        emit(StoreEventType.PROPOSAL_UPDATED, updated);
        return StoreResult.success(updated);
    }

    @Override
    public StoreResult<Void> deleteProposal(ProposalId proposalId) {
        if (proposals.remove(proposalId.value()) == null) {
            return StoreResult.failure(WorldErrorCode.PROPOSAL_NOT_FOUND, "Proposal not found: " + proposalId.value());
        }
        emit(StoreEventType.PROPOSAL_DELETED, proposalId);
        return StoreResult.success();
    }

    @Override
    public Proposal getProposal(ProposalId proposalId) {
        return proposals.get(proposalId.value());
    }

    @Override
    public boolean hasProposal(ProposalId proposalId) {
        return proposals.containsKey(proposalId.value());
    }

    @Override
    public List<Proposal> listProposals() {
        return new ArrayList<>(proposals.values());
    }

    @Override
    public List<Proposal> getEvaluatingProposals() {
        List<Proposal> results = new ArrayList<>();
        for (Proposal proposal : proposals.values()) {
            if (proposal.getStatus() == ProposalStatus.EVALUATING) {
                results.add(proposal);
            }
        }
        return results;
    }

    @Override
    public StoreResult<DecisionRecord> saveDecision(DecisionRecord decisionRecord) {
        Objects.requireNonNull(decisionRecord, "decisionRecord is required");
        String id = decisionRecord.getDecisionId().value();
        if (decisions.containsKey(id)) {
            return StoreResult.failure(WorldErrorCode.DECISION_ALREADY_EXISTS, "Decision already exists: " + id);
        }

        String proposalId = decisionRecord.getProposalId().value();
        if (decisionByProposal.containsKey(proposalId)) {
            return StoreResult.failure(
                WorldErrorCode.DECISION_ALREADY_EXISTS_FOR_PROPOSAL,
                "Decision already exists for proposal: " + proposalId
            );
        }

        decisions.put(id, decisionRecord);
        decisionByProposal.put(proposalId, id);
        emit(StoreEventType.DECISION_SAVED, decisionRecord);
        return StoreResult.success(decisionRecord);
    }

    @Override
    public DecisionRecord getDecision(DecisionId decisionId) {
        return decisions.get(decisionId.value());
    }

    @Override
    public DecisionRecord getDecisionByProposal(ProposalId proposalId) {
        String decisionId = decisionByProposal.get(proposalId.value());
        return decisionId == null ? null : decisions.get(decisionId);
    }

    @Override
    public boolean hasDecision(DecisionId decisionId) {
        return decisions.containsKey(decisionId.value());
    }

    @Override
    public StoreResult<ActorAuthorityBinding> saveBinding(ActorAuthorityBinding binding) {
        Objects.requireNonNull(binding, "binding is required");
        bindings.put(binding.getActor().getActorId(), binding);
        emit(StoreEventType.BINDING_SAVED, binding);
        return StoreResult.success(binding);
    }

    @Override
    public ActorAuthorityBinding getBinding(String actorId) {
        return bindings.get(actorId);
    }

    @Override
    public StoreResult<Void> removeBinding(String actorId) {
        if (bindings.remove(actorId) == null) {
            return StoreResult.failure(WorldErrorCode.BINDING_NOT_FOUND, "Binding not found: " + actorId);
        }
        emit(StoreEventType.BINDING_REMOVED, actorId);
        return StoreResult.success();
    }

    @Override
    public StoreStats getStats() {
        return new StoreStats(
            worlds.size(),
            edges.size(),
            proposals.size(),
            decisions.size(),
            bindings.size(),
            snapshots.size()
        );
    }

    @Override
    public Runnable subscribe(StoreEventType type, StoreEventListener listener) {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(listener, "listener is required");
        typedListeners.computeIfAbsent(type, ignored -> new CopyOnWriteArraySet<>()).add(listener);
        return () -> {
            Set<StoreEventListener> listeners = typedListeners.get(type);
            if (listeners != null) {
                listeners.remove(listener);
            }
        };
    }

    @Override
    public Runnable subscribeAll(StoreEventListener listener) {
        Objects.requireNonNull(listener, "listener is required");
        globalListeners.add(listener);
        return () -> globalListeners.remove(listener);
    }

    @Override
    public List<ActorAuthorityBinding> listBindings() {
        return new ArrayList<>(bindings.values());
    }

    @Override
    public void clear() {
        worlds.clear();
        snapshots.clear();
        edges.clear();
        proposals.clear();
        decisions.clear();
        decisionByProposal.clear();
        bindings.clear();
        genesisId = null;
    }

    private void emit(StoreEventType type, Object payload) {
        StoreEvent event = new StoreEvent(type, System.currentTimeMillis(), payload);
        Set<StoreEventListener> scoped = typedListeners.get(type);
        if (scoped != null) {
            for (StoreEventListener listener : scoped) {
                listener.onEvent(event);
            }
        }
        for (StoreEventListener listener : globalListeners) {
            listener.onEvent(event);
        }
    }
}
