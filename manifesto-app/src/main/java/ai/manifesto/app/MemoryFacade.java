package ai.manifesto.app;

import java.util.Optional;
import java.util.List;

/**
 * KR: App 레벨 메모리 저장/조회 계약입니다.
 * EN: App-level memory store/recall contract.
 */
public interface MemoryFacade {
    boolean isEnabled();

    void ingest(String key, Object value);

    Optional<Object> recall(String key);

    default RecallResult recall(RecallRequest request) {
        if (request == null || request.limit() == 0) {
            return new RecallResult(List.of());
        }
        String prefix = request.keyPrefix() == null ? "" : request.keyPrefix();
        Optional<Object> value = recall(prefix);
        if (value.isEmpty()) {
            return new RecallResult(List.of());
        }
        return new RecallResult(List.of(new StoredMemoryRecord(prefix, value.get(), System.currentTimeMillis())));
    }

    default void backfill(List<StoredMemoryRecord> records, BackfillConfig config) {
    }

    default void maintain(MemoryMaintenanceOptions options) {
    }
}
