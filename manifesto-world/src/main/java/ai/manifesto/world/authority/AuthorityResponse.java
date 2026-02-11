package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.IntentScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * KR: AuthorityResponse는 World 권한 계층에서 authority response 역할을 수행하는 구현 타입입니다.
 * EN: AuthorityResponse is an implementation type performing authority response roles in the World authority layer.
 */
public final class AuthorityResponse {
    public enum Kind {
        APPROVED,
        REJECTED,
        PENDING
    }

    public enum WaitingKind {
        HUMAN,
        TRIBUNAL,
        TIMEOUT
    }

    public static final class WaitingFor {
        private final WaitingKind kind;
        private final ActorRef delegate;
        private final List<ActorRef> members;
        private final Long until;

        private WaitingFor(WaitingKind kind, ActorRef delegate, List<ActorRef> members, Long until) {
            this.kind = Objects.requireNonNull(kind, "kind is required");
            this.delegate = delegate;
            this.members = Collections.unmodifiableList(new ArrayList<>(members != null ? members : List.of()));
            this.until = until;
        }

        public static WaitingFor human(ActorRef delegate) {
            return new WaitingFor(WaitingKind.HUMAN, Objects.requireNonNull(delegate, "delegate is required"), List.of(), null);
        }

        public static WaitingFor tribunal(List<ActorRef> members) {
            return new WaitingFor(WaitingKind.TRIBUNAL, null, members, null);
        }

        public static WaitingFor timeout(long until) {
            return new WaitingFor(WaitingKind.TIMEOUT, null, List.of(), until);
        }

        public WaitingKind getKind() {
            return kind;
        }

        public ActorRef getDelegate() {
            return delegate;
        }

        public List<ActorRef> getMembers() {
            return members;
        }

        public Long getUntil() {
            return until;
        }
    }

    private final Kind kind;
    private final IntentScope approvedScope;
    private final String reason;
    private final WaitingFor waitingFor;

    private AuthorityResponse(Kind kind, IntentScope approvedScope, String reason, WaitingFor waitingFor) {
        this.kind = Objects.requireNonNull(kind, "kind is required");
        this.approvedScope = approvedScope;
        this.reason = reason;
        this.waitingFor = waitingFor;
    }

    public static AuthorityResponse approved(IntentScope approvedScope) {
        return new AuthorityResponse(Kind.APPROVED, approvedScope, null, null);
    }

    public static AuthorityResponse rejected(String reason) {
        return new AuthorityResponse(Kind.REJECTED, null, Objects.requireNonNull(reason, "reason is required"), null);
    }

    public static AuthorityResponse pending(WaitingFor waitingFor) {
        return new AuthorityResponse(Kind.PENDING, null, null, Objects.requireNonNull(waitingFor, "waitingFor is required"));
    }

    public Kind getKind() {
        return kind;
    }

    public IntentScope getApprovedScope() {
        return approvedScope;
    }

    public String getReason() {
        return reason;
    }

    public WaitingFor getWaitingFor() {
        return waitingFor;
    }
}
