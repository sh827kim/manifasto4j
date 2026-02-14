package ai.manifesto.translator;

import ai.manifesto.intentir.DefaultIntentIrNormalizer;
import ai.manifesto.translator.pipeline.DiagnosticsAggregationPolicy;
import ai.manifesto.translator.pipeline.TranslatorPipelineOptions;
import ai.manifesto.translator.targets.json.JsonOutput;
import ai.manifesto.translator.targets.json.JsonTargetExporter;
import ai.manifesto.translator.targets.manifesto.ManifestoBundle;
import ai.manifesto.translator.targets.manifesto.ManifestoExportContext;
import ai.manifesto.translator.targets.manifesto.ManifestoTargetExporter;
import ai.manifesto.translator.targets.openapi.OpenApiSpec;
import ai.manifesto.translator.targets.openapi.OpenApiTargetExporter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultTranslatorExportIntegrationTest {

    @Test
    void translateAndExportSupportsMultipleTargetsWithDiagnosticsMerging() {
        TranslatorPipelinePlugin duplicatePlugin = new TranslatorPipelinePlugin() {
            @Override
            public int priority() {
                return 5;
            }

            @Override
            public void beforeInterpret(TranslationRequest request, List<String> diagnostics) {
                diagnostics.add("PIPE001");
                diagnostics.add("PIPE001");
            }
        };

        DefaultTranslator translator = new DefaultTranslator(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new DefaultIntentIrNormalizer()),
            List.of(duplicatePlugin),
            new TranslatorPipelineOptions(DiagnosticsAggregationPolicy.DEDUP, true)
        );

        TranslationRequest request = new TranslationRequest(
            "todo",
            "createTask",
            List.of(new TranslatorMessage("user", "create task then close task", Map.of())),
            Map.of("actor", "tester")
        );

        TranslatorExportResult<JsonOutput> jsonResult = translator.translateAndExport(request, new JsonTargetExporter(), null);
        assertNotNull(jsonResult.exported());
        assertFalse(jsonResult.executionPlan().steps().isEmpty());
        assertEquals(1, jsonResult.translationResult().diagnostics().stream().filter(d -> d.equals("PIPE001")).count());
        assertNotNull(jsonResult.graph());

        TranslatorExportResult<ManifestoBundle> manifestoResult = translator.translateAndExport(
            request,
            new ManifestoTargetExporter(),
            ManifestoExportContext.defaults()
        );
        assertNotNull(manifestoResult.exported());
        assertEquals(manifestoResult.graph().nodes().size(), manifestoResult.exported().meta().nodeCount());

        TranslatorExportResult<OpenApiSpec> openApiResult = translator.translateAndExport(
            request,
            new OpenApiTargetExporter(),
            null
        );
        assertNotNull(openApiResult.exported());
        assertEquals("3.0.3", openApiResult.exported().openapi());
        assertFalse(openApiResult.graphDiagnostics().isEmpty());
    }
}
