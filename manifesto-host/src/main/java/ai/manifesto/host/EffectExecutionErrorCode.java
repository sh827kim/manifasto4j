package ai.manifesto.host;

/**
 * KR: effect 실행 실패 분류 코드입니다.
 * EN: Classification code for effect execution failures.
 */
public enum EffectExecutionErrorCode {
    HANDLER_EXCEPTION,
    TIMEOUT,
    NULL_RESULT,
    RETRY_EXHAUSTED
}
