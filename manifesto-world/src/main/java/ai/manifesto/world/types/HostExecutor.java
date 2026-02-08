package ai.manifesto.world.types;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

public interface HostExecutor {
    HostExecutionResult execute(
            String executionKey,
            Snapshot baseSnapshot,
            Intent intent,
            HostExecutionOptions options
    );
}
