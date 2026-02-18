package ai.manifesto.runtime;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.world.schema.WorldId;

/**
 * KR: App 수명주기/액션 실행/브랜치 전환 이벤트를 관찰하는 hook 계약입니다.
 * EN: Hook contract for observing app lifecycle, action execution, and branch switch events.
 */
public interface AppHook {
    default int priority() {
        return 0;
    }

    default boolean supports(AppHookEventType eventType) {
        return true;
    }

    default AppHookErrorMode errorMode() {
        return AppHookErrorMode.CONTINUE;
    }

    default void onReady(Snapshot snapshot) {
    }

    default void onBeforeAct(Intent intent, Snapshot snapshot) {
    }

    default void onActionUpdate(Intent intent, ActionUpdate update, Snapshot snapshot) {
    }

    default void onAfterAct(Intent intent, ActionHandle handle, Snapshot snapshot) {
    }

    default void onBranchSwitched(WorldId worldId, Snapshot snapshot) {
    }
}
