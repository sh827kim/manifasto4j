package ai.manifesto.world.schema;

import java.util.Objects;

public final class IntentMeta {
    private final IntentOrigin origin;

    public IntentMeta(IntentOrigin origin) {
        this.origin = Objects.requireNonNull(origin, "origin is required");
    }

    public IntentOrigin getOrigin() {
        return origin;
    }
}
