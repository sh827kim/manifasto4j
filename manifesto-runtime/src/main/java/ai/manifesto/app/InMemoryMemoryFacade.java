package ai.manifesto.app;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KR: 메모리 기능이 활성화된 경우 사용하는 in-memory MemoryFacade 구현입니다.
 * EN: In-memory MemoryFacade implementation used when memory is enabled.
 */
public final class InMemoryMemoryFacade implements MemoryFacade {
    private final Map<String, StoredMemoryRecord> memory = new ConcurrentHashMap<>();

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
        memory.put(safeKey, new StoredMemoryRecord(safeKey, value, System.currentTimeMillis()));
    }

    @Override
    public Optional<Object> recall(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        StoredMemoryRecord record = memory.get(key.trim());
        return Optional.ofNullable(record == null ? null : record.value());
    }

    @Override
    public RecallResult recall(RecallRequest request) {
        if (request == null || request.limit() == 0) {
            return new RecallResult(List.of());
        }
        String prefix = request.keyPrefix() == null ? "" : request.keyPrefix().trim();
        int limit = request.limit() <= 0 ? Integer.MAX_VALUE : request.limit();

        List<StoredMemoryRecord> records = memory.values().stream()
            .filter(record -> prefix.isEmpty() || record.key().startsWith(prefix))
            .sorted(Comparator.comparingLong(StoredMemoryRecord::timestamp).reversed())
            .limit(limit)
            .toList();

        return new RecallResult(records);
    }

    @Override
    public void backfill(List<StoredMemoryRecord> records, BackfillConfig config) {
        if (records == null || records.isEmpty()) {
            return;
        }
        BackfillConfig safeConfig = config == null ? BackfillConfig.defaults() : config;
        for (StoredMemoryRecord record : records) {
            if (record == null || record.key() == null || record.key().isBlank()) {
                continue;
            }
            String key = record.key().trim();
            if (!safeConfig.overwriteExisting() && memory.containsKey(key)) {
                continue;
            }
            long ts = record.timestamp() <= 0 ? System.currentTimeMillis() : record.timestamp();
            memory.put(key, new StoredMemoryRecord(key, record.value(), ts));
        }
    }

    @Override
    public void maintain(MemoryMaintenanceOptions options) {
        MemoryMaintenanceOptions safe = options == null ? MemoryMaintenanceOptions.defaults() : options;
        if (safe.maxEntries() >= memory.size()) {
            return;
        }
        List<StoredMemoryRecord> sorted = memory.values().stream()
            .sorted(Comparator.comparingLong(StoredMemoryRecord::timestamp).reversed())
            .toList();

        Map<String, StoredMemoryRecord> next = new LinkedHashMap<>();
        for (int i = 0; i < safe.maxEntries() && i < sorted.size(); i++) {
            StoredMemoryRecord record = sorted.get(i);
            next.put(record.key(), record);
        }
        memory.clear();
        memory.putAll(next);
    }
}
