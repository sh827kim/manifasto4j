package ai.manifesto.sdk;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class RuntimeBackedMemoryFacade implements MemoryFacade {
    private final ai.manifesto.runtime.MemoryFacade delegate;

    RuntimeBackedMemoryFacade(ai.manifesto.runtime.MemoryFacade delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public void ingest(String key, Object value) {
        delegate.ingest(key, value);
    }

    @Override
    public Optional<Object> recall(String key) {
        return delegate.recall(key);
    }

    @Override
    public RecallResult recall(RecallRequest request) {
        ai.manifesto.runtime.RecallResult runtimeResult = delegate.recall(SdkMappings.toRuntime(request));
        return SdkMappings.toSdk(runtimeResult);
    }

    @Override
    public void backfill(List<StoredMemoryRecord> records, BackfillConfig config) {
        delegate.backfill(
            records == null ? List.of() : records.stream().map(SdkMappings::toRuntime).toList(),
            SdkMappings.toRuntime(config)
        );
    }

    @Override
    public void maintain(MemoryMaintenanceOptions options) {
        delegate.maintain(SdkMappings.toRuntime(options));
    }

    @Override
    public boolean isContextFrozen() {
        return delegate.isContextFrozen();
    }

    @Override
    public String getLastFailureMarker() {
        return delegate.getLastFailureMarker();
    }

    @Override
    public void freezeContext(String marker) {
        delegate.freezeContext(marker);
    }

    @Override
    public void unfreezeContext() {
        delegate.unfreezeContext();
    }
}
