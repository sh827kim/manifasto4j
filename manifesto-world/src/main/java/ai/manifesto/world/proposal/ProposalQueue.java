package ai.manifesto.world.proposal;

import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.IntentInstance;
import ai.manifesto.world.schema.Proposal;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.ProposalTrace;
import ai.manifesto.world.schema.WorldId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ProposalQueue {
    private final Map<String, Proposal> proposals = new LinkedHashMap<>();

    public Proposal submit(
            ProposalId proposalId,
            String executionKey,
            ActorRef actor,
            IntentInstance intent,
            WorldId baseWorld,
            ProposalTrace trace,
            long epoch,
            long submittedAt
    ) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        Objects.requireNonNull(executionKey, "executionKey is required");
        Objects.requireNonNull(actor, "actor is required");
        Objects.requireNonNull(intent, "intent is required");
        Objects.requireNonNull(baseWorld, "baseWorld is required");

        if (proposals.containsKey(proposalId.value())) {
            throw new IllegalArgumentException("proposalId already exists: " + proposalId.value());
        }

        Proposal proposal = Proposal.submitted(
                proposalId,
                executionKey,
                actor,
                intent,
                baseWorld,
                trace,
                epoch,
                submittedAt
        );
        proposals.put(proposalId.value(), proposal);
        return proposal;
    }

    public Proposal transition(ProposalId proposalId, ProposalStatus to, TransitionUpdates updates) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        Objects.requireNonNull(to, "target status is required");
        TransitionUpdates resolvedUpdates = updates != null ? updates : TransitionUpdates.empty();

        Proposal current = getOrThrow(proposalId);
        if (!ProposalStateMachine.isValidTransition(current.getStatus(), to)) {
            throw new IllegalStateException("Invalid transition: " + current.getStatus() + " -> " + to);
        }

        if (ProposalStateMachine.requiresDecision(to)
                && resolvedUpdates.getDecisionId() == null
                && current.getDecisionId() == null) {
            throw new IllegalArgumentException("Transition to " + to + " requires decisionId");
        }

        Proposal updated = current.withTransition(
                to,
                resolvedUpdates.getApprovedScope(),
                resolvedUpdates.getDecisionId(),
                resolvedUpdates.getResultWorld(),
                resolvedUpdates.getDecidedAt(),
                resolvedUpdates.getCompletedAt()
        );
        proposals.put(proposalId.value(), updated);
        return updated;
    }

    public Optional<Proposal> get(ProposalId proposalId) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        return Optional.ofNullable(proposals.get(proposalId.value()));
    }

    public Proposal getOrThrow(ProposalId proposalId) {
        return get(proposalId).orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId.value()));
    }

    public boolean has(ProposalId proposalId) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        return proposals.containsKey(proposalId.value());
    }

    public List<Proposal> getByStatus(ProposalStatus status) {
        List<Proposal> results = new ArrayList<>();
        for (Proposal proposal : proposals.values()) {
            if (proposal.getStatus() == status) {
                results.add(proposal);
            }
        }
        return results;
    }

    public List<Proposal> getByActor(String actorId) {
        List<Proposal> results = new ArrayList<>();
        for (Proposal proposal : proposals.values()) {
            if (proposal.getActor().getActorId().equals(actorId)) {
                results.add(proposal);
            }
        }
        return results;
    }

    public List<Proposal> getByBaseWorld(WorldId worldId) {
        List<Proposal> results = new ArrayList<>();
        for (Proposal proposal : proposals.values()) {
            if (proposal.getBaseWorld().equals(worldId)) {
                results.add(proposal);
            }
        }
        return results;
    }

    public List<Proposal> getTerminal() {
        List<Proposal> results = new ArrayList<>();
        for (Proposal proposal : proposals.values()) {
            if (proposal.getStatus().isTerminal()) {
                results.add(proposal);
            }
        }
        return results;
    }

    public List<Proposal> getEvaluating() {
        return getByStatus(ProposalStatus.EVALUATING);
    }

    public List<Proposal> getActive() {
        List<Proposal> results = new ArrayList<>();
        for (Proposal proposal : proposals.values()) {
            if (!proposal.getStatus().isTerminal()) {
                results.add(proposal);
            }
        }
        return results;
    }

    public List<Proposal> getIngressStage() {
        List<Proposal> results = new ArrayList<>();
        for (Proposal proposal : proposals.values()) {
            if (proposal.getStatus().isIngress()) {
                results.add(proposal);
            }
        }
        return results;
    }

    public List<Proposal> list() {
        List<Proposal> result = new ArrayList<>(proposals.values());
        result.sort(Comparator.comparingLong(Proposal::getSubmittedAt));
        return result;
    }

    public int size() {
        return proposals.size();
    }

    public boolean remove(ProposalId proposalId) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        return proposals.remove(proposalId.value()) != null;
    }

    public void clear() {
        proposals.clear();
    }
}
