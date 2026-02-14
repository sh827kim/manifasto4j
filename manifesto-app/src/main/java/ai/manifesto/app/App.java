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

    default AppSession createSession(String actorId) {
        return new DefaultAppSession(this, actorId, java.util.Map.of());
    }

    default AppSession createSession(String actorId, java.util.Map<String, Object> context) {
        return new DefaultAppSession(this, actorId, context);
    }

    default WorldId getCurrentBranchId() {
        return null;
    }

    default List<WorldId> listBranches() {
        return List.of();
    }

    default String getCurrentBranchName() {
        return null;
    }

    default List<String> listBranchNames() {
        return List.of();
    }

    default void createBranch(String branchName, WorldId worldId) {
        throw new UnsupportedOperationException("Branch aliasing is not enabled for this app");
    }

    default void switchBranch(String branchName) {
        throw new UnsupportedOperationException("Branch aliasing is not enabled for this app");
    }

    default void addHook(AppHook hook) {
    }

    default void removeHook(AppHook hook) {
    }

    default ManifestoWorld getWorld() {
        return null;
    }

    default SystemFacade getSystemFacade() {
        return new DefaultSystemFacade(this);
    }

    default MemoryFacade getMemoryFacade() {
        return new DisabledMemoryFacade();
    }

    default void switchBranch(WorldId worldId) {
        throw new UnsupportedOperationException("World integration is not enabled for this app");
    }
}
