package ai.manifesto.runtime;

/**
 * KR: App 인스턴스의 수명주기 상태입니다.
 * EN: Lifecycle status of an App instance.
 */
public enum AppStatus {
    CREATED,
    READY,
    DISPOSING,
    DISPOSED
}
