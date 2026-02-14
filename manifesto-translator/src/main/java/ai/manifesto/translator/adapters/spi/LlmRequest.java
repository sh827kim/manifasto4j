package ai.manifesto.translator.adapters.spi;

import java.util.List;

/**
 * KR: LLM completion 요청 모델입니다.
 * EN: Request model for LLM completion.
 */
public record LlmRequest(
    String system,
    List<LlmMessage> messages,
    LlmCallOptions options
) {
    public LlmRequest {
        messages = messages == null ? List.of() : List.copyOf(messages);
        options = options == null ? LlmCallOptions.defaults() : options;
    }
}
