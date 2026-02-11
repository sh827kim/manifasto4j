package ai.manifesto.world.proposal;

import ai.manifesto.world.schema.DecisionId;
import ai.manifesto.world.schema.IntentScope;
import ai.manifesto.world.schema.WorldId;

/**
 * KR: TransitionUpdates는 World 제안 처리 계층에서 transition updates 역할을 수행하는 구현 타입입니다.
 * EN: TransitionUpdates is an implementation type performing transition updates roles in the World proposal-processing layer.
 */
public final class TransitionUpdates {
    private final DecisionId decisionId;
    private final WorldId resultWorld;
    private final Long decidedAt;
    private final Long completedAt;
    private final IntentScope approvedScope;

    private TransitionUpdates(DecisionId decisionId, WorldId resultWorld, Long decidedAt, Long completedAt, IntentScope approvedScope) {
        this.decisionId = decisionId;
        this.resultWorld = resultWorld;
        this.decidedAt = decidedAt;
        this.completedAt = completedAt;
        this.approvedScope = approvedScope;
    }

    public static TransitionUpdates empty() {
        return new TransitionUpdates(null, null, null, null, null);
    }

    public TransitionUpdates withDecisionId(DecisionId value) {
        return new TransitionUpdates(value, resultWorld, decidedAt, completedAt, approvedScope);
    }

    public TransitionUpdates withResultWorld(WorldId value) {
        return new TransitionUpdates(decisionId, value, decidedAt, completedAt, approvedScope);
    }

    public TransitionUpdates withDecidedAt(Long value) {
        return new TransitionUpdates(decisionId, resultWorld, value, completedAt, approvedScope);
    }

    public TransitionUpdates withCompletedAt(Long value) {
        return new TransitionUpdates(decisionId, resultWorld, decidedAt, value, approvedScope);
    }

    public TransitionUpdates withApprovedScope(IntentScope value) {
        return new TransitionUpdates(decisionId, resultWorld, decidedAt, completedAt, value);
    }

    public DecisionId getDecisionId() {
        return decisionId;
    }

    public WorldId getResultWorld() {
        return resultWorld;
    }

    public Long getDecidedAt() {
        return decidedAt;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public IntentScope getApprovedScope() {
        return approvedScope;
    }
}
