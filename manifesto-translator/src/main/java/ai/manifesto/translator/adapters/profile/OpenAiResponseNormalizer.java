package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.LlmFinishReason;
import ai.manifesto.translator.adapters.spi.LlmResponse;
import ai.manifesto.translator.adapters.spi.LlmUsage;
import ai.manifesto.translator.adapters.spi.provider.ProviderCapabilityProfile;
import ai.manifesto.translator.adapters.spi.provider.ProviderResponseNormalizer;

import java.util.List;
import java.util.Map;

/**
 * KR: OpenAI raw 응답을 표준 응답으로 정규화합니다.
 * EN: Normalizes OpenAI-style raw response into standard LLM response.
 */
public final class OpenAiResponseNormalizer implements ProviderResponseNormalizer {
    @Override
    public LlmResponse toLlmResponse(Map<String, Object> providerResponse, ProviderCapabilityProfile profile) {
        if (providerResponse == null || providerResponse.isEmpty()) {
            return LlmResponse.error("empty response");
        }
        String content = "";
        String finishReason = null;

        Object choicesRaw = providerResponse.get("choices");
        if (choicesRaw instanceof List<?> choices && !choices.isEmpty() && choices.get(0) instanceof Map<?, ?> firstChoice) {
            Object messageRaw = ((Map<?, ?>) firstChoice).get("message");
            if (messageRaw instanceof Map<?, ?> messageMap) {
                content = asString(messageMap.get("content"));
            }
            finishReason = asString(((Map<?, ?>) firstChoice).get("finish_reason"));
        }

        LlmUsage usage = null;
        Object usageRaw = providerResponse.get("usage");
        if (usageRaw instanceof Map<?, ?> usageMap && profile.supportsUsageStats()) {
            usage = new LlmUsage(
                asInt(usageMap.get("prompt_tokens")),
                asInt(usageMap.get("completion_tokens")),
                asInt(usageMap.get("total_tokens"))
            );
        }

        return new LlmResponse(content, usage, mapFinishReason(finishReason));
    }

    private LlmFinishReason mapFinishReason(String reason) {
        if (reason == null) {
            return LlmFinishReason.ERROR;
        }
        return switch (reason) {
            case "stop" -> LlmFinishReason.STOP;
            case "length" -> LlmFinishReason.LENGTH;
            case "content_filter" -> LlmFinishReason.CONTENT_FILTER;
            default -> LlmFinishReason.ERROR;
        };
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
