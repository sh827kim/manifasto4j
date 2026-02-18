package ai.manifesto.runtime;

/**
 * KR: dispose 이후 App API를 호출할 때 발생합니다.
 * EN: Raised when App APIs are called after disposal.
 */
public final class AppDisposedException extends ManifestoAppException {
    public AppDisposedException() {
        super("APP-DISPOSED", "App is disposed");
    }
}
