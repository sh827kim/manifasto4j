package ai.manifesto.sdk;

import java.util.List;
import java.util.Optional;

/**
 * KR: SDK memory 저장/조회 계약입니다.
 * EN: SDK memory store/recall contract.
 */
public interface MemoryFacade {
    boolean isEnabled();

    void ingest(String key, Object value);

    Optional<Object> recall(String key);

    default RecallResult recall(RecallRequest request) {
        if (request == null || request.limit() == 0) {
            return new RecallResult(List.of(), isContextFrozen(), null, getLastFailureMarker());
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
}
