package ai.manifesto.translator.adapters.spi;

/**
 * KR: LLM provider 독립 포트 계약입니다.
 * EN: Provider-independent port contract for LLM completion.
 */
public interface LlmPort {
    LlmResponse complete(LlmRequest request);
}
