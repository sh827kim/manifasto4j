package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.LlmCallOptions;
import ai.manifesto.translator.adapters.spi.LlmMessage;
import ai.manifesto.translator.adapters.spi.LlmRequest;
import ai.manifesto.translator.adapters.spi.provider.ProviderCapabilityProfile;
import ai.manifesto.translator.adapters.spi.provider.ProviderRequestMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KR: 표준 요청을 Claude messages payload로 매핑합니다.
 * EN: Maps standard requests into Claude messages payload.
 */
public final class ClaudeRequestMapper implements ProviderRequestMapper {
    @Override
    public Map<String, Object> toProviderPayload(LlmRequest request, ProviderCapabilityProfile profile) {
        LlmCallOptions options = request.options() == null ? LlmCallOptions.defaults() : request.options();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", profile.defaultModel());

        if (request.system() != null && !request.system().isBlank() && profile.supportsSystemPrompt()) {
            payload.put("system", request.system());
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        for (LlmMessage message : request.messages()) {
            String safeRole = "assistant".equals(message.role()) ? "assistant" : "user";
            messages.add(Map.of(
                "role", safeRole,
                "content", message.content() == null ? "" : message.content()
            ));
        }
        payload.put("messages", messages);

        if (options.temperature() != null) {
            payload.put("temperature", options.temperature());
        }
        payload.put("max_tokens", options.maxTokens() == null ? 2048 : options.maxTokens());
        return payload;
    }
}
