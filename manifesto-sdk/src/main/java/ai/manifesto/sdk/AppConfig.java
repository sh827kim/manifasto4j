package ai.manifesto.sdk;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.EffectHandler;
import ai.manifesto.host.HostRuntime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * KR: Java SDK의 공개 App 구성 계약입니다.
 * EN: Public App configuration contract for Java SDK.
 */
public record AppConfig(
    DomainSchema schema,
    Snapshot initialSnapshot,
    HostRuntime hostRuntime,
    Map<String, Object> initialData,
    Map<String, EffectHandler> effects,
    MemoryProvider memoryProvider,
    MemoryVerifier memoryVerifier,
    boolean freezeMemoryContext
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
        return new AppConfig(schema, null, null, initialData, effects, null, null, false);
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
        return new AppConfig(schema, initialSnapshot, hostRuntime, Map.of(), Map.of(), null, null, false);
    }

    ai.manifesto.runtime.AppConfig toRuntimeConfig() {
        return new ai.manifesto.runtime.AppConfig(
            schema,
            initialSnapshot,
            hostRuntime,
            initialData,
            effects,
            null,
            null,
            null,
            null,
            null,
            null,
            toRuntimeMemoryProvider(memoryProvider),
            toRuntimeMemoryVerifier(memoryVerifier),
            freezeMemoryContext
        );
    }

    private ai.manifesto.runtime.MemoryProvider toRuntimeMemoryProvider(MemoryProvider provider) {
        if (provider == null) {
            return null;
        }
        return new ai.manifesto.runtime.MemoryProvider() {
            @Override
            public void save(ai.manifesto.runtime.StoredMemoryRecord record) {
                provider.save(new StoredMemoryRecord(record.key(), record.value(), record.timestamp()));
            }

            @Override
            public Optional<ai.manifesto.runtime.StoredMemoryRecord> load(String key) {
                return provider.load(key)
                    .map(record -> new ai.manifesto.runtime.StoredMemoryRecord(record.key(), record.value(), record.timestamp()));
            }

            @Override
            public List<ai.manifesto.runtime.StoredMemoryRecord> list() {
                return provider.list().stream()
                    .map(record -> new ai.manifesto.runtime.StoredMemoryRecord(record.key(), record.value(), record.timestamp()))
                    .toList();
            }

            @Override
            public void remove(String key) {
                provider.remove(key);
            }
        };
    }

    private ai.manifesto.runtime.MemoryVerifier toRuntimeMemoryVerifier(MemoryVerifier verifier) {
        if (verifier == null) {
            return null;
        }
        return (key, value) -> {
            MemoryVerificationResult result = verifier.verify(key, value);
            if (result == null) {
                return ai.manifesto.runtime.MemoryVerificationResult.accept();
            }
            return new ai.manifesto.runtime.MemoryVerificationResult(
                result.accepted(),
                result.freezeContext(),
                result.reason()
            );
        };
    }
}
