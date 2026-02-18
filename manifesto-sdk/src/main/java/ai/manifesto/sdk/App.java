package ai.manifesto.sdk;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * KR: Java SDK의 canonical App 인터페이스입니다.
 * EN: Canonical App interface for the Java SDK.
 */
public interface App {
    void ready() throws Exception;

    default void dispose() {
    }

    ActionHandle act(Intent intent) throws Exception;

    void subscribe(Function<Snapshot, Object> selector, Consumer<Object> handler);

    Snapshot getSnapshot();

    DomainSchema getSchema();

    default AppStatus getStatus() {
        return AppStatus.CREATED;
    }

    default String getSessionId() {
        return null;
    }

    default boolean hasSessionPersistence() {
        return false;
    }

    default List<AppHead> getHeads() {
        return List.of();
    }

    default AppHead getLatestHead() {
        return null;
    }

    default SystemFacade getSystemFacade() {
        return (systemActionType, input) -> {
            throw new UnsupportedOperationException("system facade is not available");
        };
    }

    default MemoryFacade getMemoryFacade() {
        return new MemoryFacade() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void ingest(String key, Object value) {
            }

            @Override
            public java.util.Optional<Object> recall(String key) {
                return java.util.Optional.empty();
            }
        };
    }
}
