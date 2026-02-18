package ai.manifesto.app;

/**
 * KR: 액션 실행 라이프사이클에서 관찰 가능한 phase 집합입니다.
 * EN: Observable phases in the action execution lifecycle.
 */
public enum ActionPhase {
    PREPARING,
    SUBMITTED,
    EXECUTING,
    COMPLETED,
    FAILED,
    REJECTED,
    PREPARATION_FAILED
}
