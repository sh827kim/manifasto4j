package ai.manifesto.runtime;

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

    default boolean isContextFrozen() {
        return false;
    }

    default String getLastFailureMarker() {
        return null;
    }

    default void freezeContext(String marker) {
    }

    default void unfreezeContext() {
    }

    default RecallResult recall(RecallRequest request) {
        if (request == null || request.limit() == 0) {
            return new RecallResult(List.of(), isContextFrozen(), null, getLastFailureMarker());
        }
        if (request.freezeContext()) {
            freezeContext(request.contextToken());
        }
        String prefix = request.keyPrefix() == null ? "" : request.keyPrefix();
        Optional<Object> value = recall(prefix);
        if (value.isEmpty()) {
            return new RecallResult(List.of(), isContextFrozen(), request.contextToken(), getLastFailureMarker());
        }
        return new RecallResult(
            List.of(new StoredMemoryRecord(prefix, value.get(), System.currentTimeMillis())),
            isContextFrozen(),
            request.contextToken(),
            getLastFailureMarker()
        );
    }

    default void backfill(List<StoredMemoryRecord> records, BackfillConfig config) {
    }

    default void maintain(MemoryMaintenanceOptions options) {
    }
}
