package ai.manifesto.translator.adapters.spi;

/**
 * KR: LLM 대화 메시지 단위입니다(system은 request.system으로 분리).
 * EN: LLM conversation message unit (system prompt is separated on request.system).
 */
public record LlmMessage(
    String role,
    String content
) {}
