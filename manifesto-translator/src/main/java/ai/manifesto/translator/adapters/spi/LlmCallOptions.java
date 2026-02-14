package ai.manifesto.translator.adapters.spi;

import java.util.List;

/**
 * KR: LLM 호출 옵션입니다.
 * EN: Options for LLM completion calls.
 */
public record LlmCallOptions(
    Double temperature,
    Integer maxTokens,
    List<String> stop,
    LlmResponseFormat responseFormat,
    Integer timeoutMillis
) {
    public static LlmCallOptions defaults() {
        return new LlmCallOptions(null, null, List.of(), LlmResponseFormat.TEXT, null);
    }
}
