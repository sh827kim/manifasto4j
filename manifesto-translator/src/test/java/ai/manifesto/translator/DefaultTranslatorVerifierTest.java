package ai.manifesto.translator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Test
    void verifyAppliesDomainPolicyAllowedActionsAndRequiredContext() {
        TranslatorPolicyProvider policyProvider = new InMemoryTranslatorPolicyProvider(
            Map.of(
                "todo",
                new TranslatorDomainPolicy(
                    "todo",
                    Set.of("createTask", "closeTask"),
                    Set.of("tenantId", "userId")
                )
            )
        );
        DefaultTranslatorVerifier verifier = new DefaultTranslatorVerifier(policyProvider);
        TranslationRequest request = new TranslationRequest(
            "todo",
            "deleteTask",
            List.of(new TranslatorMessage("user", "action:deleteTask", Map.of())),
            Map.of("tenantId", "t-1")
        );
        TranslationDraft draft = new TranslationDraft(
            "todo",
            "deleteTask",
            Map.of("text", "action:deleteTask"),
            Map.of(),
            List.of()
        );

        TranslationDraft verified = verifier.verify(request, draft);

        assertFalse(Boolean.TRUE.equals(verified.meta().get("verified")));
        assertTrue(verified.diagnostics().stream().anyMatch(d -> d.startsWith("TRV101")));
        assertTrue(verified.diagnostics().stream().anyMatch(d -> d.startsWith("TRV102")));
    }
}
