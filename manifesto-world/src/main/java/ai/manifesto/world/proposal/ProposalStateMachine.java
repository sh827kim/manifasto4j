package ai.manifesto.world.proposal;

import ai.manifesto.world.schema.ProposalStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * KR: ProposalStateMachine는 상태 전이 규칙을 적용해 유효한 다음 상태를 계산하는 상태 머신입니다.
 * EN: ProposalStateMachine is a state machine that applies transition rules to compute valid next states.
 */
public final class ProposalStateMachine {
    private static final Map<ProposalStatus, Set<ProposalStatus>> VALID_TRANSITIONS = new EnumMap<>(ProposalStatus.class);
    private static final Set<ProposalStatus> DECISION_REQUIRED = EnumSet.of(ProposalStatus.APPROVED, ProposalStatus.REJECTED);
    private static final Set<ProposalStatus> WORLD_CREATED = EnumSet.of(ProposalStatus.COMPLETED, ProposalStatus.FAILED);

    static {
        VALID_TRANSITIONS.put(ProposalStatus.SUBMITTED, EnumSet.of(ProposalStatus.EVALUATING, ProposalStatus.REJECTED));
        VALID_TRANSITIONS.put(ProposalStatus.EVALUATING, EnumSet.of(ProposalStatus.APPROVED, ProposalStatus.REJECTED));
        VALID_TRANSITIONS.put(ProposalStatus.APPROVED, EnumSet.of(ProposalStatus.EXECUTING));
        VALID_TRANSITIONS.put(ProposalStatus.EXECUTING, EnumSet.of(ProposalStatus.COMPLETED, ProposalStatus.FAILED));
        VALID_TRANSITIONS.put(ProposalStatus.REJECTED, EnumSet.noneOf(ProposalStatus.class));
        VALID_TRANSITIONS.put(ProposalStatus.COMPLETED, EnumSet.noneOf(ProposalStatus.class));
        VALID_TRANSITIONS.put(ProposalStatus.FAILED, EnumSet.noneOf(ProposalStatus.class));
    }

    private ProposalStateMachine() {
    }

    public static boolean isValidTransition(ProposalStatus from, ProposalStatus to) {
        return VALID_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<ProposalStatus> getValidTransitions(ProposalStatus status) {
        Set<ProposalStatus> transitions = VALID_TRANSITIONS.getOrDefault(status, Set.of());
        if (transitions.isEmpty()) {
            return Set.of();
        }
        return EnumSet.copyOf(transitions);
    }

    public static boolean requiresDecision(ProposalStatus status) {
        return DECISION_REQUIRED.contains(status);
    }

    public static boolean createsWorld(ProposalStatus status) {
        return WORLD_CREATED.contains(status);
    }
}
