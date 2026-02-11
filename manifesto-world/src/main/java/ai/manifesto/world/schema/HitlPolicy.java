package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: HitlPolicy는 권한/거버넌스 정책 구성을 표현하는 값 객체입니다.
 * EN: HitlPolicy is a value object describing authority/governance policy configuration.
 */
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
