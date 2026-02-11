package ai.manifesto.world.types;

import ai.manifesto.world.schema.ProposalId;

import java.util.Objects;

/**
 * KR: ExecutionKeys는 World 실행 경계 계층에서 execution keys 역할을 수행하는 구현 타입입니다.
 * EN: ExecutionKeys is an implementation type performing execution keys roles in the World execution-boundary layer.
 */
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
