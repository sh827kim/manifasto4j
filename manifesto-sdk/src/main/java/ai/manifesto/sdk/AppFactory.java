package ai.manifesto.sdk;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.EffectHandler;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.schema.ActorKind;

import java.util.Map;
import java.util.Objects;

/**
 * KR: Java SDK의 canonical App 생성 진입점입니다.
 * EN: Canonical App creation entrypoint for Java SDK.
 */
public final class AppFactory {
    private AppFactory() {
    }

    public static App createApp(DomainSchema schema, Map<String, EffectHandler> effects) {
        return wrap(ai.manifesto.runtime.AppFactory.createApp(schema, effects));
    }

    public static App createApp(
        DomainSchema schema,
        Map<String, Object> initialData,
        Map<String, EffectHandler> effects
    ) {
        return wrap(ai.manifesto.runtime.AppFactory.createApp(schema, initialData, effects));
    }

    public static App createApp(AppConfig config) {
        Objects.requireNonNull(config, "config is required");
        return wrap(ai.manifesto.runtime.AppFactory.createApp(config.toRuntimeConfig()));
    }

    public static App createTestApp(DomainSchema schema) {
        return wrap(ai.manifesto.runtime.AppFactory.createTestApp(schema));
    }

    public static App createTestApp(
        DomainSchema schema,
        Map<String, Object> initialData,
        Map<String, EffectHandler> effects
    ) {
        return wrap(ai.manifesto.runtime.AppFactory.createTestApp(schema, initialData, effects));
    }

    public static App createWorldApp(
        DomainSchema schema,
        Snapshot initialSnapshot,
        HostRuntime host,
        String actorId,
        ActorKind actorKind
    ) {
        return wrap(ai.manifesto.runtime.AppFactory.createWorldApp(schema, initialSnapshot, host, actorId, actorKind));
    }

    private static App wrap(ai.manifesto.runtime.App app) {
        return new RuntimeBackedApp(app);
    }
}
