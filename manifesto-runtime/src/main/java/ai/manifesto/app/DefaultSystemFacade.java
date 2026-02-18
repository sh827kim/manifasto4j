package ai.manifesto.app;

import ai.manifesto.core.Intent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * KR: App.act를 래핑해 `system.*` 타입으로 실행하는 기본 SystemFacade 구현입니다.
 * EN: Default SystemFacade implementation that wraps App.act using `system.*` intent types.
 */
public final class DefaultSystemFacade implements SystemFacade {
    private final App app;

    public DefaultSystemFacade(App app) {
        this.app = Objects.requireNonNull(app, "app is required");
    }

    @Override
    public ActionHandle act(String systemActionType, Map<String, Object> input) throws Exception {
        String normalized = Objects.requireNonNull(systemActionType, "systemActionType is required").trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("systemActionType must not be blank");
        }
        String type = normalized.startsWith("system.") ? normalized : "system." + normalized;
        return app.act(new Intent(type, input == null ? Map.of() : input, "system-" + UUID.randomUUID()));
    }
}
