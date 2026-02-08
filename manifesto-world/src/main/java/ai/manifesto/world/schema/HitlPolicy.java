package ai.manifesto.world.schema;

import java.util.Objects;

public final class HitlPolicy implements AuthorityPolicy {
    private final ActorRef delegate;
    private final Long timeoutMillis;
    private final TimeoutAction onTimeout;

    public HitlPolicy(ActorRef delegate, Long timeoutMillis, TimeoutAction onTimeout) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
        this.timeoutMillis = timeoutMillis;
        this.onTimeout = onTimeout;
    }

    public ActorRef getDelegate() {
        return delegate;
    }

    public Long getTimeoutMillis() {
        return timeoutMillis;
    }

    public TimeoutAction getOnTimeout() {
        return onTimeout;
    }

    @Override
    public AuthorityPolicyMode getMode() {
        return AuthorityPolicyMode.HITL;
    }
}
