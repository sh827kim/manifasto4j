package ai.manifesto.world.types;

import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.WorldId;

@FunctionalInterface
/**
 * KR: ExecutionKeyPolicy는 World 실행 경계 계층에서 execution key policy 계약을 정의하는 인터페이스입니다.
 * EN: ExecutionKeyPolicy is an interface defining the execution key policy contract in the World execution-boundary layer.
 */
public interface ExecutionKeyPolicy {
    String createExecutionKey(ProposalId proposalId, String actorId, WorldId baseWorld, int attempt);
}
