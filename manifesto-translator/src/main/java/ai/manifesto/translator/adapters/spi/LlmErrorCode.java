package ai.manifesto.translator.adapters.spi;

/**
 * KR: LLM 호출 실패 코드입니다.
 * EN: Error code for LLM call failures.
 */
public enum LlmErrorCode {
    AUTH_FAILED,
    RATE_LIMIT,
    INVALID_REQUEST,
    TIMEOUT,
    SERVICE_ERROR,
    NETWORK_ERROR,
    UNKNOWN
}
