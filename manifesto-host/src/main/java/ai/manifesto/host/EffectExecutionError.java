package ai.manifesto.host;

/**
 * KR: effect 실행 실패 상세 정보입니다.
 * EN: Detailed failure information for effect execution.
 */
public record EffectExecutionError(
    EffectExecutionErrorCode code,
    String message,
    int attempts,
    boolean retryable
) {}
