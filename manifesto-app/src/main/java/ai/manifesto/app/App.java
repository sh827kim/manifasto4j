package ai.manifesto.app;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.DomainSchema;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * App - server/CLI용 최소 고수준 API
 */
public interface App {
    void ready() throws Exception;

    ActionHandle act(Intent intent) throws Exception;

    void subscribe(Function<Snapshot, Object> selector, Consumer<Object> handler);

    Snapshot getSnapshot();

    DomainSchema getSchema();
}
