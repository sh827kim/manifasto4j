package ai.manifesto.app;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.schema.ActorRef;

/**
 * KR: AppConfig는 App 인스턴스 조립에 필요한 구성 옵션 계약입니다.
 * EN: AppConfig defines assembly-time configuration options for App instances.
 */
public record AppConfig(
    DomainSchema schema,
    Snapshot initialSnapshot,
    HostRuntime hostRuntime,
    ManifestoWorld world,
    ActorRef actor,
    String sessionId,
    AppSnapshotStore snapshotStore,
    AppPolicyService policyService,
    AppWorldStore worldStore
) {
}
