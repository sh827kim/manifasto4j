package ai.manifesto.runtime;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;

/**
 * KR: App 초기화/액션 전후 수명주기에 참여하는 플러그인 계약입니다.
 * EN: Plugin contract participating in App initialization and action lifecycle.
 */
public interface AppPlugin {
    default String name() {
        return getClass().getSimpleName();
    }

    default void onInit(App app) {
    }

    default void beforeAct(Intent intent, Snapshot snapshot) {
    }

    default void afterAct(Intent intent, ActionHandle handle, Snapshot snapshot) {
    }

    default void onDispose(App app) {
    }
}
