package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: FinalDecision는 World 스키마 계층에서 final decision 역할을 수행하는 구현 타입입니다.
 * EN: FinalDecision is an implementation type performing final decision roles in the World schema layer.
 */
public final class FinalDecision {
    private final FinalDecisionKind kind;
    private final String reason;
    private final TimeoutAction timeoutAction;

    private FinalDecision(FinalDecisionKind kind, String reason, TimeoutAction timeoutAction) {
        this.kind = Objects.requireNonNull(kind, "kind is required");
        this.reason = reason;
        this.timeoutAction = timeoutAction;
    }

    public static FinalDecision approved() {
        return new FinalDecision(FinalDecisionKind.APPROVED, null, null);
    }

    public static FinalDecision rejected(String reason) {
        return new FinalDecision(FinalDecisionKind.REJECTED, Objects.requireNonNull(reason, "reason is required"), null);
    }

    public static FinalDecision timeout(TimeoutAction action) {
        return new FinalDecision(FinalDecisionKind.TIMEOUT, null, Objects.requireNonNull(action, "action is required"));
    }

    public FinalDecisionKind getKind() {
        return kind;
    }

    public String getReason() {
        return reason;
    }

    public TimeoutAction getTimeoutAction() {
        return timeoutAction;
    }

    public boolean isApproved() {
        return kind == FinalDecisionKind.APPROVED || (kind == FinalDecisionKind.TIMEOUT && timeoutAction == TimeoutAction.APPROVE);
    }

    public boolean isRejected() {
        return kind == FinalDecisionKind.REJECTED || (kind == FinalDecisionKind.TIMEOUT && timeoutAction == TimeoutAction.REJECT);
    }
}
