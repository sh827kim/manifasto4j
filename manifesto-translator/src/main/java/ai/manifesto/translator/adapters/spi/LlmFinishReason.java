package ai.manifesto.translator.adapters.spi;

/**
 * KR: LLM 응답 종료 사유입니다.
 * EN: Completion finish reason from LLM providers.
 */
public enum LlmFinishReason {
    STOP,
    LENGTH,
    CONTENT_FILTER,
    ERROR
}
