package ai.manifesto.sdk;

import ai.manifesto.app.AppPolicyService;
import ai.manifesto.app.AppSnapshotStore;
import ai.manifesto.app.AppWorldStore;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.EffectHandler;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.schema.ActorRef;

import java.util.Map;

/**
 * KR: SDK 계층에서 사용하는 App 구성 계약입니다.
 * EN: App configuration contract used from the SDK layer.
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
            Map.of(),
            Map.of(),
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    ai.manifesto.app.AppConfig toRuntimeConfig() {
        return new ai.manifesto.app.AppConfig(
            schema,
            initialSnapshot,
            hostRuntime,
            initialData,
            effects,
            world,
            actor,
            sessionId,
            snapshotStore,
            policyService,
            worldStore
        );
    }
}
