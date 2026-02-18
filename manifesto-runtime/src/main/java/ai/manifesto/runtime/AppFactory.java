package ai.manifesto.runtime;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.EffectHandler;
import ai.manifesto.host.HostRuntime;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
import ai.manifesto.world.types.HostExecutionOptions;
import ai.manifesto.world.types.HostExecutionResult;
import ai.manifesto.world.types.HostExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: AppFactory는 App 인스턴스 생성 과정을 캡슐화하는 팩토리 타입입니다.
 * EN: AppFactory is a factory type that encapsulates App instance creation.
 */
public final class AppFactory {
    private AppFactory() {}

    /**
     * SDK-style createApp.
     *
     * TS SDK createApp와 동일하게 schema/effects만 받아 App을 생성합니다.
     * 초기 data는 schema default value + 빈 값 기반으로 구성됩니다.
     */
    public static App createApp(DomainSchema schema, Map<String, EffectHandler> effects) {
        return createApp(schema, Map.of(), effects);
    }

    /**
     * SDK-style createApp with initialData.
     *
     * TS SDK의 `initialData`에 대응되는 진입점입니다.
     */
    public static App createApp(
            DomainSchema schema,
            Map<String, Object> initialData,
            Map<String, EffectHandler> effects
    ) {
        Objects.requireNonNull(schema, "schema is required");
        HostRuntime host = buildHostRuntime(effects);
        Snapshot initialSnapshot = buildInitialSnapshot(schema, initialData);
        return createApp(schema, initialSnapshot, host, null, null);
    }

    /**
     * SDK-style createTestApp.
     */
    public static App createTestApp(DomainSchema schema) {
        return createApp(schema, Map.of(), Map.of());
    }

    /**
     * SDK-style createTestApp with explicit initialData/effects.
     */
    public static App createTestApp(
            DomainSchema schema,
            Map<String, Object> initialData,
            Map<String, EffectHandler> effects
    ) {
        return createApp(schema, initialData, effects);
    }

    public static App createApp(AppConfig config) {
        Objects.requireNonNull(config, "config is required");
        DomainSchema schema = Objects.requireNonNull(config.schema(), "schema is required");
        HostRuntime hostRuntime = resolveHostRuntime(config);
        Snapshot initialSnapshot = resolveInitialSnapshot(config, schema);
        return new DefaultApp(
            schema,
            initialSnapshot,
            hostRuntime,
            config.world(),
            config.actor(),
            config.sessionId(),
            config.snapshotStore(),
            config.policyService(),
            config.worldStore(),
            config.memoryProvider(),
            config.memoryVerifier(),
            config.freezeMemoryContext()
        );
    }

    public static App createApp(DomainSchema schema, Snapshot initialSnapshot, HostRuntime host) {
        return createApp(schema, initialSnapshot, host, null, null);
    }

    public static App createApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            String sessionId,
            AppSnapshotStore snapshotStore
    ) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(initialSnapshot, "initialSnapshot is required");
        Objects.requireNonNull(host, "host is required");
        return new DefaultApp(
            schema,
            initialSnapshot,
            host,
            null,
            null,
            sessionId,
            snapshotStore,
            new AllowAllPolicyService(),
            null
        );
    }

    public static App createWorldApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            String actorId,
            ActorKind actorKind
    ) {
        return createWorldApp(schema, initialSnapshot, host, actorId, actorKind, null, null);
    }

    public static App createWorldApp(
            DomainSchema schema,
            Snapshot initialSnapshot,
            HostRuntime host,
            String actorId,
            ActorKind actorKind,
            String sessionId,
            AppSnapshotStore snapshotStore
    ) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(initialSnapshot, "initialSnapshot is required");
        Objects.requireNonNull(host, "host is required");
        Objects.requireNonNull(actorId, "actorId is required");
        Objects.requireNonNull(actorKind, "actorKind is required");

        HostExecutor executor = new HostExecutor() {
            @Override
            public HostExecutionResult execute(String executionKey, Snapshot baseSnapshot, Intent intent, HostExecutionOptions options) {
                try {
                    var result = host.run(schema, baseSnapshot, intent, 5);
                    return new HostExecutionResult(
                            result.getStatus() == ai.manifesto.core.ComputeStatus.ERROR ? HostExecutionResult.Outcome.FAILED : HostExecutionResult.Outcome.COMPLETED,
                            result.getSnapshot(),
                            null,
                            result.getSnapshot().getSystem().getLastError()
                    );
                } catch (Exception e) {
                    return new HostExecutionResult(HostExecutionResult.Outcome.FAILED, baseSnapshot, null, null);
                }
            }
        };

        ManifestoWorld world = new ManifestoWorld(schema.getHash(), executor, null);
        ActorRef actor = new ActorRef(actorId, actorKind);
        world.registerActor(actor, new AutoApprovePolicy("app default actor"));

        return new DefaultApp(
            schema,
            initialSnapshot,
            host,
            world,
            actor,
            sessionId,
            snapshotStore,
            new AllowAllPolicyService(),
            new InMemoryAppWorldStore()
        );
    }

    private static HostRuntime buildHostRuntime(Map<String, EffectHandler> effects) {
        HostRuntime hostRuntime = new HostRuntime();
        if (effects == null || effects.isEmpty()) {
            return hostRuntime;
        }
        for (Map.Entry<String, EffectHandler> entry : effects.entrySet()) {
            hostRuntime.register(entry.getKey(), entry.getValue());
        }
        return hostRuntime;
    }

    private static Snapshot buildInitialSnapshot(DomainSchema schema, Map<String, Object> initialData) {
        Map<String, Object> data = new HashMap<>();
        schema.getDataFields().values().forEach(field -> {
            if (field.getDefaultValue() != null) {
                data.put(field.getFieldName(), field.getDefaultValue());
            }
        });
        if (initialData != null && !initialData.isEmpty()) {
            data.putAll(initialData);
        }
        return Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();
    }

    private static HostRuntime resolveHostRuntime(AppConfig config) {
        if (config.hostRuntime() != null) {
            return config.hostRuntime();
        }
        return buildHostRuntime(config.effects());
    }

    private static Snapshot resolveInitialSnapshot(AppConfig config, DomainSchema schema) {
        if (config.initialSnapshot() != null) {
            return config.initialSnapshot();
        }
        return buildInitialSnapshot(schema, config.initialData());
    }
}
