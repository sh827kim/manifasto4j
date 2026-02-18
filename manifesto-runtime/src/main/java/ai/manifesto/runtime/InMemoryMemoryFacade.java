package ai.manifesto.runtime;

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
    private final MemoryProvider provider;
    private final MemoryVerifier verifier;
    private volatile boolean contextFrozen;
    private volatile String contextToken;
    private volatile String lastFailureMarker;

    public InMemoryMemoryFacade() {
        this(null, null, false);
    }

    public InMemoryMemoryFacade(
        MemoryProvider provider,
        MemoryVerifier verifier,
        boolean freezeMemoryContext
    ) {
        this.provider = provider;
        this.verifier = verifier;
        this.contextFrozen = freezeMemoryContext;
        if (freezeMemoryContext) {
            this.lastFailureMarker = "context_frozen";
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void ingest(String key, Object value) {
        String safeKey = normalizeKey(key);
        if (!isIngestAllowed(safeKey, value)) {
            return;
        }
        saveRecord(new StoredMemoryRecord(safeKey, value, System.currentTimeMillis()));
    }

    @Override
    public Optional<Object> recall(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return loadRecord(key.trim()).map(StoredMemoryRecord::value);
    }

    @Override
    public RecallResult recall(RecallRequest request) {
        if (request == null || request.limit() == 0) {
            return new RecallResult(List.of(), contextFrozen, contextToken, lastFailureMarker);
        }
        if (request.freezeContext()) {
            freezeContext(request.contextToken());
        }

        String prefix = request.keyPrefix() == null ? "" : request.keyPrefix().trim();
        int limit = request.limit() <= 0 ? Integer.MAX_VALUE : request.limit();

        List<StoredMemoryRecord> records = allRecords().stream()
            .filter(record -> prefix.isEmpty() || record.key().startsWith(prefix))
            .sorted(Comparator.comparingLong(StoredMemoryRecord::timestamp).reversed())
            .limit(limit)
            .toList();

        String token = request.contextToken() != null ? request.contextToken() : contextToken;
        return new RecallResult(records, contextFrozen, token, lastFailureMarker);
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
            String key = normalizeKey(record.key());
            if (!safeConfig.overwriteExisting() && loadRecord(key).isPresent()) {
                continue;
            }
            if (!isIngestAllowed(key, record.value())) {
                continue;
            }
            long ts = record.timestamp() <= 0 ? System.currentTimeMillis() : record.timestamp();
            saveRecord(new StoredMemoryRecord(key, record.value(), ts));
        }
    }

    @Override
    public void maintain(MemoryMaintenanceOptions options) {
        MemoryMaintenanceOptions safe = options == null ? MemoryMaintenanceOptions.defaults() : options;
        List<StoredMemoryRecord> sorted = allRecords().stream()
            .sorted(Comparator.comparingLong(StoredMemoryRecord::timestamp).reversed())
            .toList();

        if (safe.maxEntries() >= sorted.size()) {
            return;
        }

        Map<String, StoredMemoryRecord> next = new LinkedHashMap<>();
        for (int i = 0; i < safe.maxEntries() && i < sorted.size(); i++) {
            StoredMemoryRecord record = sorted.get(i);
            next.put(record.key(), record);
        }
        memory.clear();
        memory.putAll(next);

        if (provider != null) {
            for (StoredMemoryRecord record : sorted) {
                if (!next.containsKey(record.key())) {
                    provider.remove(record.key());
                }
            }
        }
    }

    @Override
    public boolean isContextFrozen() {
        return contextFrozen;
    }

    @Override
    public String getLastFailureMarker() {
        return lastFailureMarker;
    }

    @Override
    public void freezeContext(String marker) {
        this.contextFrozen = true;
        this.contextToken = marker;
        if (marker != null && !marker.isBlank()) {
            this.lastFailureMarker = "context_frozen:" + marker;
            return;
        }
        if (this.lastFailureMarker == null || this.lastFailureMarker.isBlank()) {
            this.lastFailureMarker = "context_frozen";
        }
    }

    @Override
    public void unfreezeContext() {
        this.contextFrozen = false;
        this.contextToken = null;
        this.lastFailureMarker = null;
    }

    private String normalizeKey(String key) {
        String safeKey = Objects.requireNonNull(key, "key is required").trim();
        if (safeKey.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        return safeKey;
    }

    private boolean isIngestAllowed(String key, Object value) {
        if (contextFrozen) {
            if (lastFailureMarker == null) {
                lastFailureMarker = "context_frozen";
            }
            return false;
        }
        if (verifier == null) {
            return true;
        }
        MemoryVerificationResult verification = verifier.verify(key, value);
        if (verification == null) {
            return true;
        }
        if (verification.accepted()) {
            return true;
        }
        lastFailureMarker = verification.reason() == null ? "memory_verification_rejected" : verification.reason();
        if (verification.freezeContext()) {
            contextFrozen = true;
        }
        return false;
    }

    private void saveRecord(StoredMemoryRecord record) {
        memory.put(record.key(), record);
        if (provider != null) {
            provider.save(record);
        }
    }

    private Optional<StoredMemoryRecord> loadRecord(String key) {
        StoredMemoryRecord inMemory = memory.get(key);
        if (inMemory != null) {
            return Optional.of(inMemory);
        }
        if (provider == null) {
            return Optional.empty();
        }
        Optional<StoredMemoryRecord> loaded = provider.load(key);
        loaded.ifPresent(record -> memory.put(record.key(), record));
        return loaded;
    }

    private List<StoredMemoryRecord> allRecords() {
        if (provider == null) {
            return List.copyOf(memory.values());
        }
        Map<String, StoredMemoryRecord> merged = new LinkedHashMap<>();
        for (StoredMemoryRecord record : provider.list()) {
            if (record != null && record.key() != null && !record.key().isBlank()) {
                merged.put(record.key(), record);
                memory.putIfAbsent(record.key(), record);
            }
        }
        merged.putAll(memory);
        return List.copyOf(merged.values());
    }
}
