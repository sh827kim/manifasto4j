package ai.manifesto.runtime;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.world.schema.WorldId;

/**
 * KR: AppRef는 plugin/hook가 App 상태를 읽을 때 사용하는 안정된 조회 포트입니다.
 * EN: AppRef is a stable read port used by plugins/hooks to inspect App state.
 */
public interface AppRef {
    AppStatus getStatus();

    Snapshot getSnapshot();

    DomainSchema getSchema();

    WorldId getCurrentBranchId();

    String getCurrentBranchName();
}
