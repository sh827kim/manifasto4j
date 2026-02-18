package ai.manifesto.runtime;

/**
 * KR: 단일 액션 실행 중 특정 시점의 phase 전이 이벤트를 나타냅니다.
 * EN: Represents a phase transition event at a specific point during a single action execution.
 */
public record ActionUpdate(
    ActionPhase phase,
    String message,
    long timestampMillis
) {}
