package ai.manifesto.app;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.world.ManifestoWorld;
import ai.manifesto.world.schema.WorldId;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * KR: App는 준비(ready), 액션 실행(act), 구독(subscribe) API를 노출하는 최상위 애플리케이션 인터페이스입니다.
 * EN: App is the top-level application interface exposing ready, act, and subscribe APIs.
 */
public interface App {
    void ready() throws Exception;

    ActionHandle act(Intent intent) throws Exception;

    void subscribe(Function<Snapshot, Object> selector, Consumer<Object> handler);

    Snapshot getSnapshot();

    DomainSchema getSchema();

    default String getSessionId() {
        return null;
    }

    default boolean hasSessionPersistence() {
        return false;
    }

    default WorldId getCurrentBranchId() {
        return null;
    }

    default List<WorldId> listBranches() {
        return List.of();
    }

    default void addHook(AppHook hook) {
    }

    default void removeHook(AppHook hook) {
    }

    default ManifestoWorld getWorld() {
        return null;
    }

    default void switchBranch(WorldId worldId) {
        throw new UnsupportedOperationException("World integration is not enabled for this app");
    }
}
