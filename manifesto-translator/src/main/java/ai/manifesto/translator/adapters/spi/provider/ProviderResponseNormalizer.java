package ai.manifesto.translator.adapters.spi.provider;

import ai.manifesto.translator.adapters.spi.LlmResponse;

import java.util.Map;

/**
 * KR: provider raw 응답을 표준 LLM 응답으로 정규화하는 계약입니다.
 * EN: Contract that normalizes provider raw responses into standard LLM responses.
 */
public interface ProviderResponseNormalizer {
    LlmResponse toLlmResponse(Map<String, Object> providerResponse, ProviderCapabilityProfile profile);
}
