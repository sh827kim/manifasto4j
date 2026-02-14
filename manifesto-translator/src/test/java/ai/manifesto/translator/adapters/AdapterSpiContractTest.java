package ai.manifesto.translator.adapters;

import ai.manifesto.translator.adapters.profile.ProviderBindings;
import ai.manifesto.translator.adapters.spi.*;
import ai.manifesto.translator.adapters.spi.provider.ProviderAdapterBinding;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdapterSpiContractTest {

    @Test
    void openAiBindingMapsRequestAndNormalizesResponse() {
        ProviderAdapterBinding binding = ProviderBindings.openAi();
        LlmRequest request = new LlmRequest(
            "system prompt",
            List.of(new LlmMessage("user", "hello")),
            new LlmCallOptions(0.2, 256, List.of("END"), LlmResponseFormat.JSON, 30_000)
        );

        Map<String, Object> payload = binding.toProviderPayload(request);
        assertEquals("openai", binding.profile().providerId());
        assertEquals("gpt-4o-mini", payload.get("model"));
        assertTrue(payload.containsKey("messages"));
        assertTrue(payload.containsKey("response_format"));

        Map<String, Object> providerResponse = Map.of(
            "choices", List.of(Map.of(
                "message", Map.of("content", "ok"),
                "finish_reason", "stop"
            )),
            "usage", Map.of(
                "prompt_tokens", 11,
                "completion_tokens", 7,
                "total_tokens", 18
            )
        );
        LlmResponse normalized = binding.normalizeResponse(providerResponse);
        assertEquals("ok", normalized.content());
        assertEquals(LlmFinishReason.STOP, normalized.finishReason());
        assertNotNull(normalized.usage());
        assertEquals(18, normalized.usage().totalTokens());
    }

    @Test
    void ollamaAndClaudeBindingsExposeProviderSpecificShape() {
        LlmRequest request = new LlmRequest(
            "be concise",
            List.of(new LlmMessage("user", "create task")),
            new LlmCallOptions(0.1, 100, List.of(), LlmResponseFormat.TEXT, null)
        );

        ProviderAdapterBinding ollama = ProviderBindings.ollama();
        Map<String, Object> ollamaPayload = ollama.toProviderPayload(request);
        assertEquals("ollama", ollama.profile().providerId());
        assertTrue(String.valueOf(ollamaPayload.get("prompt")).contains("System: be concise"));

        LlmResponse ollamaResponse = ollama.normalizeResponse(Map.of("response", "done"));
        assertEquals("done", ollamaResponse.content());
        assertEquals(LlmFinishReason.STOP, ollamaResponse.finishReason());
        assertNull(ollamaResponse.usage());

        ProviderAdapterBinding claude = ProviderBindings.claude();
        Map<String, Object> claudePayload = claude.toProviderPayload(request);
        assertEquals("claude", claude.profile().providerId());
        assertTrue(claudePayload.containsKey("system"));
        assertTrue(claudePayload.containsKey("messages"));

        LlmResponse claudeResponse = claude.normalizeResponse(Map.of(
            "content", List.of(Map.of("type", "text", "text", "completed")),
            "stop_reason", "end_turn",
            "usage", Map.of("input_tokens", 3, "output_tokens", 5)
        ));
        assertEquals("completed", claudeResponse.content());
        assertEquals(LlmFinishReason.STOP, claudeResponse.finishReason());
        assertNotNull(claudeResponse.usage());
        assertEquals(8, claudeResponse.usage().totalTokens());
    }
}
