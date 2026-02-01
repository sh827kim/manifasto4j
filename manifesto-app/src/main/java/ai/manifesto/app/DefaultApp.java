package ai.manifesto.app;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.HostRuntime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * DefaultApp - server/CLI용 최소 App 구현체
 */
public final class DefaultApp implements App {
    private final DomainSchema schema;
    private final HostRuntime host;
    private Snapshot snapshot;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public DefaultApp(DomainSchema schema, Snapshot initialSnapshot, HostRuntime host) {
        this.schema = Objects.requireNonNull(schema, "schema is required");
        this.snapshot = Objects.requireNonNull(initialSnapshot, "snapshot is required");
        this.host = Objects.requireNonNull(host, "host is required");
    }

    @Override
    public void ready() {
        // no-op (explicit initialization hook)
    }

    @Override
    public ActionHandle act(Intent intent) throws Exception {
        ComputeResult result = host.run(schema, snapshot, intent, 5);
        snapshot = result.getSnapshot();
        notifySubscribers(snapshot);
        return new ActionHandle(result);
    }

    @Override
    public void subscribe(Function<Snapshot, Object> selector, Consumer<Object> handler) {
        subscriptions.add(new Subscription(selector, handler));
        handler.accept(selector.apply(snapshot));
    }

    @Override
    public DomainSchema getSchema() {
        return schema;
    }

    @Override
    public Snapshot getSnapshot() {
        return snapshot;
    }

    private void notifySubscribers(Snapshot snapshot) {
        for (Subscription sub : subscriptions) {
            Object value = sub.selector.apply(snapshot);
            sub.handler.accept(value);
        }
    }

    private record Subscription(Function<Snapshot, Object> selector, Consumer<Object> handler) {}
}
