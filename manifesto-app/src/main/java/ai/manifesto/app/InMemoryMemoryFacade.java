package ai.manifesto.app;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KR: 메모리 기능이 활성화된 경우 사용하는 in-memory MemoryFacade 구현입니다.
 * EN: In-memory MemoryFacade implementation used when memory is enabled.
 */
public final class InMemoryMemoryFacade implements MemoryFacade {
    private final Map<String, Object> memory = new ConcurrentHashMap<>();

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void ingest(String key, Object value) {
        String safeKey = Objects.requireNonNull(key, "key is required").trim();
        if (safeKey.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        memory.put(safeKey, value);
    }

    @Override
    public Optional<Object> recall(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(memory.get(key.trim()));
    }
}
