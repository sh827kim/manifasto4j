package ai.manifesto.translator.adapters.profile;

import ai.manifesto.translator.adapters.spi.provider.ProviderAdapterBinding;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KR: OpenAI/Ollama/Claude provider 바인딩 팩토리입니다.
 * EN: Factory for OpenAI/Ollama/Claude provider adapter bindings.
 */
public final class ProviderBindings {
    private ProviderBindings() {
    }

    public static ProviderAdapterBinding openAi() {
        return new ProviderAdapterBinding(
            ProviderProfiles.openAi(),
            new OpenAiRequestMapper(),
            new OpenAiResponseNormalizer()
        );
    }

    public static ProviderAdapterBinding ollama() {
        return new ProviderAdapterBinding(
            ProviderProfiles.ollama(),
            new OllamaRequestMapper(),
            new OllamaResponseNormalizer()
        );
    }

    public static ProviderAdapterBinding claude() {
        return new ProviderAdapterBinding(
            ProviderProfiles.claude(),
            new ClaudeRequestMapper(),
            new ClaudeResponseNormalizer()
        );
    }

    public static Map<String, ProviderAdapterBinding> all() {
        Map<String, ProviderAdapterBinding> bindings = new LinkedHashMap<>();
        bindings.put("openai", openAi());
        bindings.put("ollama", ollama());
        bindings.put("claude", claude());
        return Map.copyOf(bindings);
    }
}
