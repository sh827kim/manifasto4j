package ai.manifesto.translator;

import ai.manifesto.intentir.DefaultIntentIrLexicon;
import ai.manifesto.intentir.DefaultIntentIrLowerer;
import ai.manifesto.intentir.DefaultIntentIrResolver;
import ai.manifesto.intentir.IntentIrDocument;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslatorPipelineTest {

    @Test
    void pipelineInvokesPluginHooksInOrder() {
        List<String> calls = new ArrayList<>();
        TranslatorPipelinePlugin plugin = new TranslatorPipelinePlugin() {
            @Override
            public void beforeInterpret(TranslationRequest request, List<String> diagnostics) {
                calls.add("beforeInterpret");
                diagnostics.add("PLG001: before interpret");
            }

            @Override
            public TranslationDraft afterInterpret(TranslationRequest request, TranslationDraft draft, List<String> diagnostics) {
                calls.add("afterInterpret");
                return draft;
            }

            @Override
            public void beforeVerify(TranslationRequest request, TranslationDraft draft, List<String> diagnostics) {
                calls.add("beforeVerify");
            }

            @Override
            public TranslationDraft afterVerify(TranslationRequest request, TranslationDraft verifiedDraft, List<String> diagnostics) {
                calls.add("afterVerify");
                return verifiedDraft;
            }

            @Override
            public void beforeRefine(TranslationRequest request, TranslationDraft draft, List<String> diagnostics) {
                calls.add("beforeRefine");
            }

            @Override
            public IntentIrDocument afterRefine(TranslationRequest request, TranslationDraft draft, IntentIrDocument intentIr, List<String> diagnostics) {
                calls.add("afterRefine");
                diagnostics.add("PLG002: after refine");
                return intentIr;
            }
        };

        DefaultTranslator translator = new DefaultTranslator(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new ai.manifesto.intentir.DefaultIntentIrNormalizer()),
            List.of(plugin)
        );

        TranslationResult result = translator.translate(new TranslationRequest(
            "todo",
            "createTask",
            List.of(new TranslatorMessage("user", "please create task", Map.of())),
            Map.of()
        ));

        assertEquals(List.of(
            "beforeInterpret",
            "afterInterpret",
            "beforeVerify",
            "afterVerify",
            "beforeRefine",
            "afterRefine"
        ), calls);
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("PLG001")));
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("PLG002")));
    }

    @Test
    void intentIrResolutionPluginRepairsUnknownActionUsingHint() {
        var resolver = new DefaultIntentIrResolver(
            Map.of("todo", Set.of("createTask"))
        );
        var lexicon = new DefaultIntentIrLexicon(
            Map.of("todo", Set.of("createTask"))
        );
        IntentIrResolutionPlugin resolutionPlugin = new IntentIrResolutionPlugin(resolver, lexicon);

        DefaultTranslator translator = new DefaultTranslator(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new ai.manifesto.intentir.DefaultIntentIrNormalizer()),
            List.of(resolutionPlugin)
        );

        TranslationResult result = translator.translate(new TranslationRequest(
            "todo",
            null,
            List.of(new TranslatorMessage("assistant", "no user message", Map.of())),
            Map.of()
        ));

        assertEquals("createTask", result.intentIr().action());
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("RSV002")));
        assertTrue(Boolean.TRUE.equals(result.intentIr().meta().get("lexiconValid")));
    }

    @Test
    void intentIrResolutionPluginCanApplyLowerer() {
        var resolver = new DefaultIntentIrResolver(
            Map.of("todo", Set.of("createTask"))
        );
        var lexicon = new DefaultIntentIrLexicon(
            Map.of("todo", Set.of("createTask"))
        );
        var lowerer = new DefaultIntentIrLowerer();
        IntentIrResolutionPlugin resolutionPlugin = new IntentIrResolutionPlugin(resolver, lexicon, lowerer);

        DefaultTranslator translator = new DefaultTranslator(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new ai.manifesto.intentir.DefaultIntentIrNormalizer()),
            List.of(resolutionPlugin)
        );

        TranslationResult result = translator.translate(new TranslationRequest(
            "todo",
            "createTask",
            List.of(new TranslatorMessage("assistant", "no user message", Map.of())),
            Map.of()
        ));

        assertEquals("createTask", result.intentIr().action());
        assertTrue(result.intentIr().input().containsKey("_intentIr.domain"));
        assertTrue(Boolean.TRUE.equals(result.intentIr().meta().get("lowered")));
    }
}
