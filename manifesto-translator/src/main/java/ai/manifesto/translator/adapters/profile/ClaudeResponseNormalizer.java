package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.LlmFinishReason;
import ai.manifesto.translator.adapters.spi.LlmResponse;
import ai.manifesto.translator.adapters.spi.LlmUsage;
import ai.manifesto.translator.adapters.spi.provider.ProviderCapabilityProfile;
import ai.manifesto.translator.adapters.spi.provider.ProviderResponseNormalizer;

import java.util.List;
import java.util.Map;

/**
 * KR: Claude raw 응답을 표준 응답으로 정규화합니다.
 * EN: Normalizes Claude raw response into standard LLM response.
 */
public final class ClaudeResponseNormalizer implements ProviderResponseNormalizer {
    @Override
    public LlmResponse toLlmResponse(Map<String, Object> providerResponse, ProviderCapabilityProfile profile) {
        if (providerResponse == null || providerResponse.isEmpty()) {
            return LlmResponse.error("empty response");
        }

        String content = "";
        Object contentRaw = providerResponse.get("content");
        if (contentRaw instanceof List<?> blocks) {
            StringBuilder builder = new StringBuilder();
            for (Object block : blocks) {
                if (block instanceof Map<?, ?> map && "text".equals(String.valueOf(map.get("type")))) {
                    Object text = map.get("text");
                    if (text != null) {
                        builder.append(text);
                    }
                }
            }
            content = builder.toString();
        }

        String stopReason = String.valueOf(providerResponse.getOrDefault("stop_reason", ""));
        LlmUsage usage = null;
        Object usageRaw = providerResponse.get("usage");
        if (usageRaw instanceof Map<?, ?> usageMap && profile.supportsUsageStats()) {
            int prompt = asInt(usageMap.get("input_tokens"));
            int completion = asInt(usageMap.get("output_tokens"));
            usage = new LlmUsage(prompt, completion, prompt + completion);
        }

        return new LlmResponse(content, usage, mapFinishReason(stopReason));
    }

    private LlmFinishReason mapFinishReason(String reason) {
        return switch (reason) {
            case "end_turn", "stop" -> LlmFinishReason.STOP;
            case "max_tokens" -> LlmFinishReason.LENGTH;
            case "content_filter" -> LlmFinishReason.CONTENT_FILTER;
            default -> LlmFinishReason.ERROR;
        };
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
