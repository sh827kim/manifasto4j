package ai.manifesto.sdk;

import ai.manifesto.app.App;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.EffectHandler;

import java.util.Map;
import java.util.Objects;

/**
 * KR: Java SDK의 canonical App 생성 진입점입니다.
 * EN: Canonical App creation entrypoint for the Java SDK.
 */
public final class AppFactory {
    private AppFactory() {
    }

    public static App createApp(DomainSchema schema, Map<String, EffectHandler> effects) {
        return ai.manifesto.app.AppFactory.createApp(schema, effects);
    }

    public static App createApp(
        DomainSchema schema,
        Map<String, Object> initialData,
        Map<String, EffectHandler> effects
    ) {
        return ai.manifesto.app.AppFactory.createApp(schema, initialData, effects);
    }

    public static App createApp(AppConfig config) {
        Objects.requireNonNull(config, "config is required");
        return ai.manifesto.app.AppFactory.createApp(config.toRuntimeConfig());
    }

    public static App createTestApp(DomainSchema schema) {
        return ai.manifesto.app.AppFactory.createTestApp(schema);
    }

    public static App createTestApp(
        DomainSchema schema,
        Map<String, Object> initialData,
        Map<String, EffectHandler> effects
    ) {
        return ai.manifesto.app.AppFactory.createTestApp(schema, initialData, effects);
    }
}
