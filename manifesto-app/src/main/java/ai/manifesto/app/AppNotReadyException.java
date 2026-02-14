package ai.manifesto.app;

/**
 * KR: App이 ready 이전 상태에서 실행 API가 호출될 때 발생합니다.
 * EN: Raised when execution APIs are called before App is ready.
 */
public final class AppNotReadyException extends ManifestoAppException {
    public AppNotReadyException() {
        super("APP-NOT-READY", "App is not ready. Call ready() before act().");
    }
}
