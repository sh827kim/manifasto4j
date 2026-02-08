package ai.manifesto.world.schema;

import java.util.Objects;

public final class IntentOrigin {
    private final String projectionId;
    private final IntentSource source;
    private final ActorRef actor;

    public IntentOrigin(String projectionId, IntentSource source, ActorRef actor) {
        this.projectionId = Objects.requireNonNull(projectionId, "projectionId is required");
        this.source = Objects.requireNonNull(source, "source is required");
        this.actor = Objects.requireNonNull(actor, "actor is required");
    }

    public String getProjectionId() {
        return projectionId;
    }

    public IntentSource getSource() {
        return source;
    }

    public ActorRef getActor() {
        return actor;
    }
}
