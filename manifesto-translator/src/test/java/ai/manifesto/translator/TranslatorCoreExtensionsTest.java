package ai.manifesto.translator;

import ai.manifesto.translator.core.Chunk;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.core.ResolutionStatus;
import ai.manifesto.translator.helpers.ExecutionPlanBuilder;
import ai.manifesto.translator.helpers.TranslatorChunkValidator;
import ai.manifesto.translator.helpers.TranslatorGraphValidator;
import ai.manifesto.translator.invariants.AbstractDependencyChecker;
import ai.manifesto.translator.invariants.CausalIntegrityChecker;
import ai.manifesto.translator.invariants.CompletenessChecker;
import ai.manifesto.translator.invariants.StatefulnessChecker;
import ai.manifesto.translator.pipeline.DiagnosticsAggregationPolicy;
import ai.manifesto.translator.pipeline.TranslatorPipelineOptions;
import ai.manifesto.translator.strategies.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslatorCoreExtensionsTest {

    @Test
    void strategyComposerBuildsExecutionPlan() {
        StrategyComposer composer = new StrategyComposer(
            new SentenceWindowDecomposeStrategy(),
            new DeterministicGraphTranslateStrategy(),
            new AggressiveMergeStrategy()
        );
        TranslationRequest request = new TranslationRequest(
            "todo",
            null,
            List.of(new TranslatorMessage("user", "create task. then close task.", Map.of())),
            Map.of()
        );

        var plan = composer.compose(
            request,
            DecomposeOptions.defaults(),
            TranslateOptions.defaults(),
            MergeOptions.aggressive()
        );

        assertFalse(plan.steps().isEmpty());
        assertEquals(0, plan.steps().get(0).order());
    }

    @Test
    void validatorsAndInvariantsDetectGraphHealth() {
        IntentGraph graph = new IntentGraph(
            List.of(
                new IntentGraphNode("n1", "state.set", Map.of(), ResolutionStatus.RESOLVED),
                new IntentGraphNode("n2", "state.get", Map.of(), ResolutionStatus.RESOLVED)
            ),
            List.of(new ai.manifesto.translator.core.DependencyEdge("n1", "n2")),
            Map.of()
        );

        assertTrue(new TranslatorGraphValidator().validate(graph).valid());
        assertTrue(new CompletenessChecker().isComplete(graph));
        assertTrue(new StatefulnessChecker().isStateful(graph));
        assertFalse(new CausalIntegrityChecker().hasCycle(graph));
        assertFalse(new AbstractDependencyChecker().hasAbstractDependency(graph));

        var plan = new ExecutionPlanBuilder().build(graph);
        assertEquals(2, plan.steps().size());
    }

    @Test
    void chunkValidatorRejectsOverlap() {
        TranslatorChunkValidator validator = new TranslatorChunkValidator();
        List<Chunk> chunks = List.of(
            new Chunk("c1", "hello", new ai.manifesto.translator.core.Span(0, 5)),
            new Chunk("c2", "world", new ai.manifesto.translator.core.Span(4, 9))
        );
        var result = validator.validate(chunks);
        assertFalse(result.valid());
        assertTrue(result.diagnostics().stream().anyMatch(code -> code.startsWith("CHV004")));
    }

    @Test
    void pipelineSupportsPluginPriorityAndDiagnosticsPolicy() {
        StringBuilder order = new StringBuilder();
        TranslatorPipelinePlugin high = new TranslatorPipelinePlugin() {
            @Override
            public int priority() {
                return 10;
            }

            @Override
            public TranslatorPluginType type() {
                return TranslatorPluginType.INSPECTOR;
            }

            @Override
            public void beforeInterpret(TranslationRequest request, List<String> diagnostics) {
                order.append("H");
                diagnostics.add("DUP001");
            }
        };
        TranslatorPipelinePlugin low = new TranslatorPipelinePlugin() {
            @Override
            public int priority() {
                return 1;
            }

            @Override
            public void beforeInterpret(TranslationRequest request, List<String> diagnostics) {
                order.append("L");
                diagnostics.add("DUP001");
            }
        };

        DefaultTranslator translator = new DefaultTranslator(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new ai.manifesto.intentir.DefaultIntentIrNormalizer()),
            List.of(low, high),
            new TranslatorPipelineOptions(DiagnosticsAggregationPolicy.DEDUP, true)
        );

        TranslationResult result = translator.translate(new TranslationRequest(
            "todo",
            "createTask",
            List.of(new TranslatorMessage("user", "create task", Map.of())),
            Map.of()
        ));

        assertEquals("HL", order.toString());
        assertEquals(1, result.diagnostics().stream().filter(d -> d.equals("DUP001")).count());
    }
}
