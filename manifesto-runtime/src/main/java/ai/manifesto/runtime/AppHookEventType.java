package ai.manifesto.runtime;

/**
 * KR: Hook 필터링에 사용하는 App 이벤트 타입입니다.
 * EN: App event types used for hook filtering.
 */
public enum AppHookEventType {
    READY,
    BEFORE_ACT,
    ACTION_UPDATE,
    AFTER_ACT,
    BRANCH_SWITCHED
}
