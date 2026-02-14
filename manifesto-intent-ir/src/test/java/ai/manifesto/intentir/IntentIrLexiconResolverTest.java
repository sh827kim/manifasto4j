package ai.manifesto.intentir;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntentIrLexiconResolverTest {

    @Test
    void lexiconRejectsUnknownAction() {
        DefaultIntentIrLexicon lexicon = new DefaultIntentIrLexicon(
            Map.of("todo", Set.of("add", "remove"))
        );
        IntentIrDocument document = new IntentIrDocument(
            "1.0.0",
            "todo",
            "update",
            Map.of(),
            Map.of()
        );

        IntentIrLexiconCheckResult result = lexicon.check(document);
        assertFalse(result.valid());
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("LXC004")));
    }

    @Test
    void resolverUsesActionHintWhenUnknown() {
        DefaultIntentIrResolver resolver = new DefaultIntentIrResolver(
            Map.of("todo", Set.of("add", "remove"))
        );
        IntentIrDocument document = new IntentIrDocument(
            "1.0.0",
            "todo",
            "unknown",
            Map.of("text", "please add item"),
            Map.of("actionHint", "add")
        );

        IntentIrResolveResult result = resolver.resolve(document);
        assertEquals("add", result.document().action());
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("RSV001")));
    }

    @Test
    void lexiconCanValidateRequiredInputAndMetaKeys() {
        DefaultIntentIrLexicon lexicon = new DefaultIntentIrLexicon(
            Map.of(
                "todo",
                new IntentIrLexiconPolicy(
                    Set.of("add"),
                    Set.of("title"),
                    Set.of("tenantId")
                )
            ),
            true
        );
        IntentIrDocument document = new IntentIrDocument(
            "1.0.0",
            "todo",
            "add",
            Map.of(),
            Map.of()
        );
        IntentIrLexiconCheckResult result = lexicon.check(document);
        assertFalse(result.valid());
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("LXC005")));
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("LXC006")));
    }

    @Test
    void resolverCanUseDiscourseAndDetectHintConflict() {
        DefaultIntentIrResolver resolver = new DefaultIntentIrResolver(
            Map.of("todo", Set.of("add", "remove"))
        );
        IntentIrResolveResult result = resolver.resolve(new IntentIrDocument(
            "1.0.0",
            "todo",
            "unknown",
            Map.of("text", "do it"),
            Map.of(
                "actionHint", "remove",
                "focusAction", "add",
                "discourseActions", List.of("add", "remove")
            )
        ));

        assertEquals("add", result.document().action());
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("RSV005")));
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("RSV007")));
    }
}
