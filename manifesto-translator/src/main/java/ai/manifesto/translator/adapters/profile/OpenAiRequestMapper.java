package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.LlmCallOptions;
import ai.manifesto.translator.adapters.spi.LlmMessage;
import ai.manifesto.translator.adapters.spi.LlmRequest;
import ai.manifesto.translator.adapters.spi.LlmResponseFormat;
import ai.manifesto.translator.adapters.spi.provider.ProviderCapabilityProfile;
import ai.manifesto.translator.adapters.spi.provider.ProviderRequestMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KR: 표준 요청을 OpenAI chat completion payload 형식으로 매핑합니다.
 * EN: Maps standard requests into OpenAI-style chat completion payload.
 */
public final class OpenAiRequestMapper implements ProviderRequestMapper {
    @Override
    public Map<String, Object> toProviderPayload(LlmRequest request, ProviderCapabilityProfile profile) {
        LlmCallOptions options = request.options() == null ? LlmCallOptions.defaults() : request.options();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", profile.defaultModel());

        List<Map<String, Object>> messages = new ArrayList<>();
        if (request.system() != null && !request.system().isBlank() && profile.supportsSystemPrompt()) {
            messages.add(Map.of("role", "system", "content", request.system()));
        }
        for (LlmMessage message : request.messages()) {
            messages.add(Map.of(
                "role", safeRole(message.role()),
                "content", message.content() == null ? "" : message.content()
            ));
        }
        payload.put("messages", messages);

        if (options.temperature() != null) {
            payload.put("temperature", options.temperature());
        }
        if (options.maxTokens() != null) {
            payload.put("max_tokens", options.maxTokens());
        }
        if (options.stop() != null && !options.stop().isEmpty() && profile.supportsStopSequences()) {
            payload.put("stop", List.copyOf(options.stop()));
        }
        if (options.responseFormat() == LlmResponseFormat.JSON && profile.supportsJsonResponseFormat()) {
            payload.put("response_format", Map.of("type", "json_object"));
        }
        return payload;
    }

    private String safeRole(String role) {
        if (role == null || role.isBlank()) {
            return "user";
        }
        return switch (role) {
            case "user", "assistant", "system", "tool" -> role;
            default -> "user";
        };
    }
}
