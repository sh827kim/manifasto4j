package ai.manifesto.app;

import java.util.Optional;

/**
 * KR: App 레벨 메모리 저장/조회 계약입니다.
 * EN: App-level memory store/recall contract.
 */
public interface MemoryFacade {
    boolean isEnabled();

    void ingest(String key, Object value);

    Optional<Object> recall(String key);
}
