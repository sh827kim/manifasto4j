package ai.manifesto.sdk;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

final class RuntimeBackedApp implements App {
    private final ai.manifesto.runtime.App delegate;

    RuntimeBackedApp(ai.manifesto.runtime.App delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    @Override
    public void ready() throws Exception {
        delegate.ready();
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }

    @Override
    public ActionHandle act(Intent intent) throws Exception {
        return new ActionHandle(delegate.act(intent));
    }

    @Override
    public void subscribe(Function<Snapshot, Object> selector, Consumer<Object> handler) {
        delegate.subscribe(selector, handler);
    }

    @Override
    public Snapshot getSnapshot() {
        return delegate.getSnapshot();
    }

    @Override
    public DomainSchema getSchema() {
        return delegate.getSchema();
    }

    @Override
    public AppStatus getStatus() {
        return SdkMappings.toSdk(delegate.getStatus());
    }

    @Override
    public String getSessionId() {
        return delegate.getSessionId();
    }

    @Override
    public boolean hasSessionPersistence() {
        return delegate.hasSessionPersistence();
    }

    @Override
    public List<AppHead> getHeads() {
        return delegate.getHeads().stream()
            .map(SdkMappings::toSdk)
            .toList();
    }

    @Override
    public AppHead getLatestHead() {
        return SdkMappings.toSdk(delegate.getLatestHead());
    }

    @Override
    public SystemFacade getSystemFacade() {
        return new RuntimeBackedSystemFacade(delegate.getSystemFacade());
    }

    @Override
    public MemoryFacade getMemoryFacade() {
        return new RuntimeBackedMemoryFacade(delegate.getMemoryFacade());
    }
}
