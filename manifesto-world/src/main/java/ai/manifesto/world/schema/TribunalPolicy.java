package ai.manifesto.world.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * KR: TribunalPolicy는 권한/거버넌스 정책 구성을 표현하는 값 객체입니다.
 * EN: TribunalPolicy is a value object describing authority/governance policy configuration.
 */
public final class TribunalPolicy implements AuthorityPolicy {
    private final List<ActorRef> members;
    private final QuorumRule quorum;
    private final Long timeoutMillis;
    private final TimeoutAction onTimeout;

    public TribunalPolicy(List<ActorRef> members, QuorumRule quorum, Long timeoutMillis, TimeoutAction onTimeout) {
        this.members = Collections.unmodifiableList(new ArrayList<>(members != null ? members : List.of()));
        this.quorum = Objects.requireNonNull(quorum, "quorum is required");
        this.timeoutMillis = timeoutMillis;
        this.onTimeout = onTimeout;
    }

    public List<ActorRef> getMembers() {
        return members;
    }

    public QuorumRule getQuorum() {
        return quorum;
    }

    public Long getTimeoutMillis() {
        return timeoutMillis;
    }

    public TimeoutAction getOnTimeout() {
        return onTimeout;
    }

    @Override
    public AuthorityPolicyMode getMode() {
        return AuthorityPolicyMode.TRIBUNAL;
    }
}
