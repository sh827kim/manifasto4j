package ai.manifesto.world.persistence;

import ai.manifesto.core.Snapshot;
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

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * KR: WorldStore는 저장/조회 경계를 정의하는 저장소 인터페이스입니다.
 * EN: WorldStore is a repository interface defining persistence and query boundaries.
 */
public interface WorldStore {
    StoreResult<World> saveWorld(World world);
    default StoreBatchResult saveWorldBatch(List<World> worlds) {
        return StoreBatching.apply(worlds, this::saveWorld);
    }
    World getWorld(WorldId worldId);
    boolean hasWorld(WorldId worldId);
    List<World> listWorlds();
    default List<World> listWorlds(WorldQuery query) {
        WorldQuery safeQuery = query == null ? WorldQuery.defaults() : query;
        return listWorlds().stream()
            .filter(world -> world != null)
            .filter(world -> safeQuery.schemaHash() == null || safeQuery.schemaHash().equals(world.getSchemaHash()))
            .filter(world -> safeQuery.createdAfterInclusive() == null || world.getCreatedAt() >= safeQuery.createdAfterInclusive())
            .filter(world -> safeQuery.createdBeforeInclusive() == null || world.getCreatedAt() <= safeQuery.createdBeforeInclusive())
            .sorted(safeQuery.sortCreatedAtDesc()
                ? Comparator.comparingLong(World::getCreatedAt).reversed()
                : Comparator.comparingLong(World::getCreatedAt))
            .skip(safeQuery.safeOffset())
            .limit(safeQuery.safeLimit() <= 0 ? Long.MAX_VALUE : safeQuery.safeLimit())
            .collect(Collectors.toList());
    }
    World getGenesis();
    StoreResult<Void> setGenesis(WorldId worldId);

    StoreResult<Void> saveSnapshot(WorldId worldId, Snapshot snapshot);
    Snapshot getSnapshot(WorldId worldId);

    StoreResult<WorldEdge> saveEdge(WorldEdge edge);
    default StoreBatchResult saveEdgeBatch(List<WorldEdge> worldEdges) {
        return StoreBatching.apply(worldEdges, this::saveEdge);
    }
    WorldEdge getEdge(EdgeId edgeId);
    WorldEdge getParentEdge(WorldId worldId);
    List<WorldEdge> getChildEdges(WorldId worldId);
    List<WorldEdge> listEdges();
    default List<WorldEdge> listEdges(EdgeQuery query) {
        EdgeQuery safeQuery = query == null ? EdgeQuery.defaults() : query;
        return listEdges().stream()
            .filter(edge -> edge != null)
            .filter(edge -> safeQuery.fromWorldId() == null || safeQuery.fromWorldId().equals(edge.getFromWorld().value()))
            .filter(edge -> safeQuery.toWorldId() == null || safeQuery.toWorldId().equals(edge.getToWorld().value()))
            .filter(edge -> safeQuery.proposalId() == null || safeQuery.proposalId().equals(edge.getProposalId().value()))
            .filter(edge -> safeQuery.decisionId() == null || safeQuery.decisionId().equals(edge.getDecisionId().value()))
            .limit(safeQuery.safeLimit() <= 0 ? Long.MAX_VALUE : safeQuery.safeLimit())
            .collect(Collectors.toList());
    }

    StoreResult<Proposal> saveProposal(Proposal proposal);
    default StoreBatchResult saveProposalBatch(List<Proposal> proposals) {
        return StoreBatching.apply(proposals, this::saveProposal);
    }
    StoreResult<Proposal> updateProposal(ProposalId proposalId, TransitionUpdates updates, ProposalStatus nextStatus);
    StoreResult<Void> deleteProposal(ProposalId proposalId);
    Proposal getProposal(ProposalId proposalId);
    boolean hasProposal(ProposalId proposalId);
    List<Proposal> listProposals();
    default List<Proposal> listProposals(ProposalQuery query) {
        ProposalQuery safeQuery = query == null ? ProposalQuery.defaults() : query;
        return listProposals().stream()
            .filter(proposal -> proposal != null)
            .filter(proposal -> safeQuery.statuses() == null || safeQuery.statuses().isEmpty()
                || safeQuery.statuses().contains(proposal.getStatus()))
            .filter(proposal -> safeQuery.actorId() == null || safeQuery.actorId().equals(proposal.getActor().getActorId()))
            .filter(proposal -> safeQuery.baseWorldId() == null || safeQuery.baseWorldId().equals(proposal.getBaseWorld().value()))
            .filter(proposal -> safeQuery.submittedAfterInclusive() == null || proposal.getSubmittedAt() >= safeQuery.submittedAfterInclusive())
            .filter(proposal -> safeQuery.submittedBeforeInclusive() == null || proposal.getSubmittedAt() <= safeQuery.submittedBeforeInclusive())
            .sorted(safeQuery.sortSubmittedAtDesc()
                ? Comparator.comparingLong(Proposal::getSubmittedAt).reversed()
                : Comparator.comparingLong(Proposal::getSubmittedAt))
            .skip(safeQuery.safeOffset())
            .limit(safeQuery.safeLimit() <= 0 ? Long.MAX_VALUE : safeQuery.safeLimit())
            .collect(Collectors.toList());
    }
    List<Proposal> getEvaluatingProposals();
    default List<Proposal> listProposalsByStatus(ProposalStatus status) {
        if (status == null) {
            return List.of();
        }
        return listProposals().stream()
            .filter(proposal -> proposal != null && proposal.getStatus() == status)
            .collect(Collectors.toList());
    }

    StoreResult<DecisionRecord> saveDecision(DecisionRecord decisionRecord);
    DecisionRecord getDecision(DecisionId decisionId);
    DecisionRecord getDecisionByProposal(ProposalId proposalId);
    boolean hasDecision(DecisionId decisionId);

    StoreResult<ActorAuthorityBinding> saveBinding(ActorAuthorityBinding binding);
    ActorAuthorityBinding getBinding(String actorId);
    StoreResult<Void> removeBinding(String actorId);
    List<ActorAuthorityBinding> listBindings();
    default StoreStats getStats() {
        return new StoreStats(
            listWorlds().size(),
            listEdges().size(),
            listProposals().size(),
            listProposals().size() == 0 ? 0 : listProposals().stream().map(Proposal::getDecisionId).filter(java.util.Objects::nonNull).collect(Collectors.toSet()).size(),
            listBindings().size(),
            listWorlds().stream().map(World::getWorldId).map(this::getSnapshot).filter(java.util.Objects::nonNull).toList().size()
        );
    }

    void clear();
}
