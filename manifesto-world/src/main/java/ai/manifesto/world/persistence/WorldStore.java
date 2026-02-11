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

/**
 * KR: WorldStore는 저장/조회 경계를 정의하는 저장소 인터페이스입니다.
 * EN: WorldStore is a repository interface defining persistence and query boundaries.
 */
public interface WorldStore {
    StoreResult<World> saveWorld(World world);
    World getWorld(WorldId worldId);
    boolean hasWorld(WorldId worldId);
    List<World> listWorlds();
    World getGenesis();
    StoreResult<Void> setGenesis(WorldId worldId);

    StoreResult<Void> saveSnapshot(WorldId worldId, Snapshot snapshot);
    Snapshot getSnapshot(WorldId worldId);

    StoreResult<WorldEdge> saveEdge(WorldEdge edge);
    WorldEdge getEdge(EdgeId edgeId);
    WorldEdge getParentEdge(WorldId worldId);
    List<WorldEdge> getChildEdges(WorldId worldId);
    List<WorldEdge> listEdges();

    StoreResult<Proposal> saveProposal(Proposal proposal);
    StoreResult<Proposal> updateProposal(ProposalId proposalId, TransitionUpdates updates, ProposalStatus nextStatus);
    StoreResult<Void> deleteProposal(ProposalId proposalId);
    Proposal getProposal(ProposalId proposalId);
    boolean hasProposal(ProposalId proposalId);
    List<Proposal> listProposals();
    List<Proposal> getEvaluatingProposals();

    StoreResult<DecisionRecord> saveDecision(DecisionRecord decisionRecord);
    DecisionRecord getDecision(DecisionId decisionId);
    DecisionRecord getDecisionByProposal(ProposalId proposalId);
    boolean hasDecision(DecisionId decisionId);

    StoreResult<ActorAuthorityBinding> saveBinding(ActorAuthorityBinding binding);
    ActorAuthorityBinding getBinding(String actorId);
    StoreResult<Void> removeBinding(String actorId);
    List<ActorAuthorityBinding> listBindings();

    void clear();
}
