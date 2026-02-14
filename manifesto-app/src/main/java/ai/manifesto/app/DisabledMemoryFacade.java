package ai.manifesto.app;

import java.util.Optional;

/**
 * KR: 메모리 기능이 비활성화된 경우 사용하는 no-op MemoryFacade 구현입니다.
 * EN: No-op MemoryFacade implementation used when memory is disabled.
 */
public final class DisabledMemoryFacade implements MemoryFacade {
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void ingest(String key, Object value) {
        // intentionally no-op
    }

    @Override
    public Optional<Object> recall(String key) {
        return Optional.empty();
    }
}
