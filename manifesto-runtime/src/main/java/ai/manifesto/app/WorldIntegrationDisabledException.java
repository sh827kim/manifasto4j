package ai.manifesto.app;

/**
 * KR: world 통합 기능이 비활성화된 App에서 world API를 호출할 때 발생합니다.
 * EN: Raised when world APIs are called on an App without world integration.
 */
public final class WorldIntegrationDisabledException extends ManifestoAppException {
    public WorldIntegrationDisabledException() {
        super("APP-WORLD-INTEGRATION-DISABLED", "World integration is not enabled for this app");
    }
}
