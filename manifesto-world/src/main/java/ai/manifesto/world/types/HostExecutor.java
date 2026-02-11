package ai.manifesto.world.types;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

/**
 * KR: HostExecutor는 World 실행 경계 계층에서 host executor 계약을 정의하는 인터페이스입니다.
 * EN: HostExecutor is an interface defining the host executor contract in the World execution-boundary layer.
 */
public interface HostExecutor {
    HostExecutionResult execute(
            String executionKey,
            Snapshot baseSnapshot,
            Intent intent,
            HostExecutionOptions options
    );
}
