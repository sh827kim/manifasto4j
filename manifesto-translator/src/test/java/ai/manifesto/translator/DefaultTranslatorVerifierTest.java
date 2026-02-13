package ai.manifesto.translator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTranslatorVerifierTest {

    @Test
    void verifyMarksDraftAsVerifiedWhenNoVerifierIssues() {
        DefaultTranslatorVerifier verifier = new DefaultTranslatorVerifier();
        TranslationRequest request = new TranslationRequest(
            "todo",
            "createTask",
            List.of(new TranslatorMessage("user", "create one", Map.of())),
            Map.of()
        );
        TranslationDraft draft = new TranslationDraft(
            "todo",
            "createTask",
            Map.of("text", "create one", "messageCount", 1),
            Map.of(),
            List.of()
        );

        TranslationDraft verified = verifier.verify(request, draft);

        assertTrue(Boolean.TRUE.equals(verified.meta().get("verified")));
        assertEquals(1.0d, (double) verified.meta().get("verificationScore"));
        assertTrue(verified.diagnostics().isEmpty());
    }

    @Test
    void verifyAddsDiagnosticsAndLowersScoreWhenPolicyViolationsExist() {
        DefaultTranslatorVerifier verifier = new DefaultTranslatorVerifier();
        TranslationRequest request = new TranslationRequest(
            "todo",
            null,
            List.of(new TranslatorMessage("assistant", "no user", Map.of())),
            Map.of()
        );
        TranslationDraft draft = new TranslationDraft(
            "todo",
            "unknown",
            Map.of("text", ""),
            Map.of(),
            List.of("TRI001: user message is missing")
        );

        TranslationDraft verified = verifier.verify(request, draft);

        assertFalse(Boolean.TRUE.equals(verified.meta().get("verified")));
        assertTrue((double) verified.meta().get("verificationScore") < 1.0d);
        assertTrue(verified.diagnostics().stream().anyMatch(d -> d.startsWith("TRV002")));
        assertTrue(verified.diagnostics().stream().anyMatch(d -> d.startsWith("TRV004")));
        assertTrue(verified.diagnostics().stream().anyMatch(d -> d.startsWith("TRV006")));
    }
}
