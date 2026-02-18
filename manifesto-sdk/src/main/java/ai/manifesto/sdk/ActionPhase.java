package ai.manifesto.sdk;

/**
 * KR: SDK 액션 진행 단계입니다.
 * EN: SDK action lifecycle phase.
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
