package ai.manifesto.translator.adapters.spi;

/**
 * KR: LLM 토큰 사용량입니다.
 * EN: LLM token usage metrics.
 */
public record LlmUsage(
    int promptTokens,
    int completionTokens,
    int totalTokens
) {}
