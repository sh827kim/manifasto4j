package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.LlmCallOptions;
import ai.manifesto.translator.adapters.spi.LlmMessage;
import ai.manifesto.translator.adapters.spi.LlmRequest;
import ai.manifesto.translator.adapters.spi.provider.ProviderCapabilityProfile;
import ai.manifesto.translator.adapters.spi.provider.ProviderRequestMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KR: 표준 요청을 Ollama generate payload로 매핑합니다.
 * EN: Maps standard requests into Ollama generate payload.
 */
public final class OllamaRequestMapper implements ProviderRequestMapper {
    @Override
    public Map<String, Object> toProviderPayload(LlmRequest request, ProviderCapabilityProfile profile) {
        LlmCallOptions options = request.options() == null ? LlmCallOptions.defaults() : request.options();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", profile.defaultModel());
        payload.put("prompt", buildPrompt(request));
        payload.put("stream", false);
        if (options.temperature() != null) {
            payload.put("options", Map.of("temperature", options.temperature()));
        }
        return payload;
    }

    private String buildPrompt(LlmRequest request) {
        StringBuilder builder = new StringBuilder();
        if (request.system() != null && !request.system().isBlank()) {
            builder.append("System: ").append(request.system()).append("\n\n");
        }
        for (LlmMessage message : request.messages()) {
            builder
                .append((message.role() == null ? "user" : message.role()).toUpperCase())
                .append(": ")
                .append(message.content() == null ? "" : message.content())
                .append("\n\n");
        }
        return builder.toString().trim();
    }
}
