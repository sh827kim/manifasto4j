package ai.manifesto.translator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTranslatorTest {

    @Test
    void translateUsesActionHintWhenProvided() {
        DefaultTranslator translator = new DefaultTranslator();
        TranslationRequest request = new TranslationRequest(
            "todo",
            "createTask",
            List.of(new TranslatorMessage("user", "please add task", Map.of())),
            Map.of("requestId", "req-1")
        );

        TranslationResult result = translator.translate(request);

        assertEquals("todo", result.intentIr().domain());
        assertEquals("createTask", result.intentIr().action());
        assertEquals("please add task", result.intentIr().input().get("text"));
        assertEquals("req-1", result.intentIr().meta().get("requestId"));
        assertTrue(Boolean.TRUE.equals(result.intentIr().meta().get("verified")));
    }

    @Test
    void translateInfersActionFromUserMessagePattern() {
        DefaultTranslator translator = new DefaultTranslator();
        TranslationRequest request = new TranslationRequest(
            "todo",
            null,
            List.of(new TranslatorMessage("user", "action:approveTask please process", Map.of())),
            Map.of()
        );

        TranslationResult result = translator.translate(request);

        assertEquals("approveTask", result.intentIr().action());
        assertTrue(result.diagnostics().isEmpty());
    }

    @Test
    void translateProducesDiagnosticsWhenActionCannotBeResolved() {
        DefaultTranslator translator = new DefaultTranslator();
        TranslationRequest request = new TranslationRequest(
            "todo",
            null,
            List.of(new TranslatorMessage("assistant", "no user message", Map.of())),
            Map.of()
        );

        TranslationResult result = translator.translate(request);

        assertEquals("unknown", result.intentIr().action());
        assertFalse(result.diagnostics().isEmpty());
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.startsWith("TRI001")));
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.startsWith("TRV002")));
    }
}
