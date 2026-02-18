package ai.manifesto.sdk;

import java.util.Map;
import java.util.Objects;

final class RuntimeBackedSystemFacade implements SystemFacade {
    private final ai.manifesto.runtime.SystemFacade delegate;

    RuntimeBackedSystemFacade(ai.manifesto.runtime.SystemFacade delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    @Override
    public ActionHandle act(String systemActionType, Map<String, Object> input) throws Exception {
        return new ActionHandle(delegate.act(systemActionType, input));
    }
}
