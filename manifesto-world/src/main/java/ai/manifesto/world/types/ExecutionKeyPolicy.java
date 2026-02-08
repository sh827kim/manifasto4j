package ai.manifesto.world.types;

import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.WorldId;

@FunctionalInterface
public interface ExecutionKeyPolicy {
    String createExecutionKey(ProposalId proposalId, String actorId, WorldId baseWorld, int attempt);
}
