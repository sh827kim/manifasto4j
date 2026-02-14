package ai.manifesto.translator;

import ai.manifesto.translator.pipeline.DiagnosticsAggregationPolicy;
import ai.manifesto.translator.pipeline.TranslatorPipelineOptions;
import ai.manifesto.translator.plugins.TranslatorPluginPresets;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslatorConformancePluginsTest {

    @Test
    void conformancePluginPresetEmitsExpectedDiagnostics() {
        DefaultTranslator translator = new DefaultTranslator(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new ai.manifesto.intentir.DefaultIntentIrNormalizer()),
            TranslatorPluginPresets.conformanceSet(),
            new TranslatorPipelineOptions(DiagnosticsAggregationPolicy.DEDUP, true)
        );

        TranslationResult result = translator.translate(new TranslationRequest(
            "todo",
            null,
            List.of(new TranslatorMessage("user", "1. create task\n2. close task or postpone", Map.of())),
            Map.of()
        ));

        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("PLG-OR-001")));
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("PLG-TASK-001")));
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("PLG-DEP-001")));
        assertTrue(Boolean.TRUE.equals(result.intentIr().meta().get("dependencyRepairApplied")));
    }

    @Test
    void diagnosticsPolicyCanPreserveDuplicates() {
        TranslatorPipelinePlugin duplicatePlugin = new TranslatorPipelinePlugin() {
            @Override
            public void beforeInterpret(TranslationRequest request, List<String> diagnostics) {
                diagnostics.add("PLG-DUP-001");
            }

            @Override
            public TranslationDraft afterInterpret(TranslationRequest request, TranslationDraft draft, List<String> diagnostics) {
                diagnostics.add("PLG-DUP-001");
                return draft;
            }
        };

        DefaultTranslator translator = new DefaultTranslator(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new ai.manifesto.intentir.DefaultIntentIrNormalizer()),
            List.of(duplicatePlugin),
            new TranslatorPipelineOptions(DiagnosticsAggregationPolicy.PRESERVE, true)
        );

        TranslationResult result = translator.translate(new TranslationRequest(
            "todo",
            null,
            List.of(new TranslatorMessage("user", "action: createTask", Map.of())),
            Map.of()
        ));

        long count = result.diagnostics().stream().filter(d -> d.equals("PLG-DUP-001")).count();
        assertTrue(count >= 2);
    }
}
