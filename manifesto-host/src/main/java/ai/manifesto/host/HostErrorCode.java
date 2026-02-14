package ai.manifesto.host;

/**
 * KR: Host 런타임 계층의 표준 에러 코드입니다.
 * EN: Standard error codes for the Host runtime layer.
 */
public enum HostErrorCode {
    INVALID_ARGUMENT,
    INTENT_ID_MISSING,
    HANDLER_NOT_FOUND,
    EFFECT_EXECUTION_FAILED,
    APPLY_FAILED,
    ITERATION_LIMIT_EXCEEDED,
    UNKNOWN
}
