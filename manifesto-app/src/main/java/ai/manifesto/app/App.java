package ai.manifesto.app;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.schema.WorldId;

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

    default ManifestoWorld getWorld() {
        return null;
    }

    default void switchBranch(WorldId worldId) {
        throw new UnsupportedOperationException("World integration is not enabled for this app");
    }
}
