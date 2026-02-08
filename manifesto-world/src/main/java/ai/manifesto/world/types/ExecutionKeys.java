package ai.manifesto.world.types;

import ai.manifesto.world.schema.ProposalId;

import java.util.Objects;

public final class ExecutionKeys {
    private ExecutionKeys() {
    }

    public static String createExecutionKey(ProposalId proposalId, int attempt) {
        Objects.requireNonNull(proposalId, "proposalId is required");
        if (attempt <= 0) {
            throw new IllegalArgumentException("attempt must be greater than zero");
        }
        return proposalId.value() + ":" + attempt;
    }
}
