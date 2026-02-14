package ai.manifesto.translator.adapters.spi;

/**
 * KR: LLM completion 응답 모델입니다.
 * EN: Response model for LLM completion.
 */
public record LlmResponse(
    String content,
    LlmUsage usage,
    LlmFinishReason finishReason
) {
    public static LlmResponse error(String message) {
        return new LlmResponse(message, null, LlmFinishReason.ERROR);
    }
}
