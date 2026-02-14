package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.LlmFinishReason;
import ai.manifesto.translator.adapters.spi.LlmResponse;
import ai.manifesto.translator.adapters.spi.provider.ProviderCapabilityProfile;
import ai.manifesto.translator.adapters.spi.provider.ProviderResponseNormalizer;

import java.util.Map;

/**
 * KR: Ollama raw 응답을 표준 응답으로 정규화합니다.
 * EN: Normalizes Ollama raw response into standard LLM response.
 */
public final class OllamaResponseNormalizer implements ProviderResponseNormalizer {
    @Override
    public LlmResponse toLlmResponse(Map<String, Object> providerResponse, ProviderCapabilityProfile profile) {
        if (providerResponse == null || providerResponse.isEmpty()) {
            return LlmResponse.error("empty response");
        }
        String content = "";
        Object response = providerResponse.get("response");
        if (response != null) {
            content = String.valueOf(response);
        }
        return new LlmResponse(content, null, LlmFinishReason.STOP);
    }
}
