package ai.manifesto.app;

import ai.manifesto.core.Intent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * KR: App 위에서 actor/context를 고정해 act를 호출하는 기본 세션 구현입니다.
 * EN: Default session implementation that invokes app actions with fixed actor/context.
 */
public final class DefaultAppSession implements AppSession {
    private final App app;
    private final String actorId;
    private final Map<String, Object> context;

    public DefaultAppSession(App app, String actorId, Map<String, Object> context) {
        this.app = Objects.requireNonNull(app, "app is required");
        this.actorId = Objects.requireNonNull(actorId, "actorId is required");
        this.context = Map.copyOf(context == null ? Map.of() : context);
    }

    @Override
    public String actorId() {
        return actorId;
    }

    @Override
    public Map<String, Object> context() {
        return context;
    }

    @Override
    public ActionHandle act(String actionType, Map<String, Object> input) throws Exception {
        Map<String, Object> payload = input == null ? Map.of() : new LinkedHashMap<>(input);
        String intentId = actorId + "-" + UUID.randomUUID();
        return app.act(new Intent(actionType, payload, intentId));
    }

    @Override
    public AppSession withContext(String key, Object value) {
        Objects.requireNonNull(key, "key is required");
        Map<String, Object> next = new LinkedHashMap<>(context);
        next.put(key, value);
        return new DefaultAppSession(app, actorId, next);
    }
}
