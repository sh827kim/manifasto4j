package ai.manifesto.app;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.EffectHandler;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.schema.ActorRef;

import java.util.Map;

/**
 * KR: AppConfig는 App 인스턴스 조립에 필요한 구성 옵션 계약입니다.
 * EN: AppConfig defines assembly-time configuration options for App instances.
 */
public record AppConfig(
    DomainSchema schema,
    Snapshot initialSnapshot,
    HostRuntime hostRuntime,
    Map<String, Object> initialData,
    Map<String, EffectHandler> effects,
    ManifestoWorld world,
    ActorRef actor,
    String sessionId,
    AppSnapshotStore snapshotStore,
    AppPolicyService policyService,
    AppWorldStore worldStore
) {
    public AppConfig {
        initialData = initialData != null ? Map.copyOf(initialData) : Map.of();
        effects = effects != null ? Map.copyOf(effects) : Map.of();
    }

    /**
     * Backward-compatible constructor for legacy app-style config.
     */
    public AppConfig(
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
        this(
            schema,
            initialSnapshot,
            hostRuntime,
            Map.of(),
            Map.of(),
            world,
            actor,
            sessionId,
            snapshotStore,
            policyService,
            worldStore
        );
    }

    /**
     * SDK-style config constructor.
     */
    public static AppConfig sdk(
        DomainSchema schema,
        Map<String, Object> initialData,
        Map<String, EffectHandler> effects
    ) {
        return new AppConfig(
            schema,
            null,
            null,
            initialData,
            effects,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static AppConfig sdk(
        DomainSchema schema,
        Map<String, EffectHandler> effects
    ) {
        return sdk(schema, Map.of(), effects);
    }

    public static AppConfig legacy(
        DomainSchema schema,
        Snapshot initialSnapshot,
        HostRuntime hostRuntime
    ) {
        return new AppConfig(
            schema,
            initialSnapshot,
            hostRuntime,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
