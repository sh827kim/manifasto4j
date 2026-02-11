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

public final class MemoryWorldStore implements WorldStore {
    private final Map<String, World> worlds = new LinkedHashMap<>();
    private final Map<String, Snapshot> snapshots = new HashMap<>();
    private final Map<String, WorldEdge> edges = new LinkedHashMap<>();
    private final Map<String, Proposal> proposals = new LinkedHashMap<>();
    private final Map<String, DecisionRecord> decisions = new LinkedHashMap<>();
    private final Map<String, String> decisionByProposal = new HashMap<>();
    private final Map<String, ActorAuthorityBinding> bindings = new LinkedHashMap<>();

    private WorldId genesisId;

    @Override
    public StoreResult<World> saveWorld(World world) {
        if (worlds.containsKey(world.getWorldId().value())) {
            return StoreResult.failure("World already exists: " + world.getWorldId().value());
        }
        worlds.put(world.getWorldId().value(), world);
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
            return StoreResult.failure("Genesis already set");
        }
        if (!worlds.containsKey(worldId.value())) {
            return StoreResult.failure("World does not exist: " + worldId.value());
        }
        genesisId = worldId;
        return StoreResult.success();
    }

    @Override
    public StoreResult<Void> saveSnapshot(WorldId worldId, Snapshot snapshot) {
        if (!worlds.containsKey(worldId.value())) {
            return StoreResult.failure("World does not exist: " + worldId.value());
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
            return StoreResult.failure("Edge already exists: " + edgeId);
        }
        if (!worlds.containsKey(edge.getFromWorld().value())) {
            return StoreResult.failure("Source world does not exist: " + edge.getFromWorld().value());
        }
        if (!worlds.containsKey(edge.getToWorld().value())) {
            return StoreResult.failure("Target world does not exist: " + edge.getToWorld().value());
        }
        edges.put(edgeId, edge);
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
            return StoreResult.failure("Proposal already exists: " + proposal.getProposalId().value());
        }
        proposals.put(proposal.getProposalId().value(), proposal);
        return StoreResult.success(proposal);
    }

    @Override
    public StoreResult<Proposal> updateProposal(ProposalId proposalId, TransitionUpdates updates, ProposalStatus nextStatus) {
        Proposal proposal = proposals.get(proposalId.value());
        if (proposal == null) {
            return StoreResult.failure("Proposal not found: " + proposalId.value());
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
        return StoreResult.success(updated);
    }

    @Override
    public StoreResult<Void> deleteProposal(ProposalId proposalId) {
        if (proposals.remove(proposalId.value()) == null) {
            return StoreResult.failure("Proposal not found: " + proposalId.value());
        }
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
            return StoreResult.failure("Decision already exists: " + id);
        }

        String proposalId = decisionRecord.getProposalId().value();
        if (decisionByProposal.containsKey(proposalId)) {
            return StoreResult.failure("Decision already exists for proposal: " + proposalId);
        }

        decisions.put(id, decisionRecord);
        decisionByProposal.put(proposalId, id);
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
        return StoreResult.success(binding);
    }

    @Override
    public ActorAuthorityBinding getBinding(String actorId) {
        return bindings.get(actorId);
    }

    @Override
    public StoreResult<Void> removeBinding(String actorId) {
        if (bindings.remove(actorId) == null) {
            return StoreResult.failure("Binding not found: " + actorId);
        }
        return StoreResult.success();
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
}
