package ai.manifesto.translator;

import ai.manifesto.intentir.DefaultIntentIrNormalizer;
import ai.manifesto.translator.helpers.TranslatorGraphValidator;
import ai.manifesto.translator.invariants.TranslatorInvariantSuite;
import ai.manifesto.translator.pipeline.TranslatorPipelineOptions;
import ai.manifesto.translator.strategies.*;
import ai.manifesto.translator.targets.ExportInput;
import ai.manifesto.translator.targets.TargetExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * KR: interpret -> verify -> refine 단계를 순차 실행하는 기본 Translator 구현입니다.
 * EN: Default Translator implementation that runs interpret -> verify -> refine stages sequentially.
 */
public final class DefaultTranslator implements Translator {
    private final TranslatorPipeline pipeline;
    private final StrategyComposer strategyComposer;
    private final TranslatorGraphValidator graphValidator;
    private final TranslatorInvariantSuite invariantSuite;

    public DefaultTranslator() {
        this(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new DefaultIntentIrNormalizer()),
            List.of()
        );
    }

    public DefaultTranslator(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner
    ) {
        this(interpreter, verifier, refiner, List.of());
    }

    public DefaultTranslator(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner,
        List<TranslatorPipelinePlugin> plugins
    ) {
        this(interpreter, verifier, refiner, plugins, TranslatorPipelineOptions.defaults());
    }

    public DefaultTranslator(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner,
        List<TranslatorPipelinePlugin> plugins,
        TranslatorPipelineOptions options
    ) {
        this(
            interpreter,
            verifier,
            refiner,
            plugins,
            options,
            new StrategyComposer(
                new SentenceWindowDecomposeStrategy(),
                new DeterministicGraphTranslateStrategy(),
                new ConservativeMergeStrategy()
            ),
            new TranslatorGraphValidator(),
            new TranslatorInvariantSuite()
        );
    }

    public DefaultTranslator(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner,
        List<TranslatorPipelinePlugin> plugins,
        TranslatorPipelineOptions options,
        StrategyComposer strategyComposer,
        TranslatorGraphValidator graphValidator,
        TranslatorInvariantSuite invariantSuite
    ) {
        this.pipeline = new TranslatorPipeline(
            Objects.requireNonNull(interpreter, "interpreter must not be null"),
            Objects.requireNonNull(verifier, "verifier must not be null"),
            Objects.requireNonNull(refiner, "refiner must not be null"),
            plugins == null ? List.of() : plugins,
            options
        );
        this.strategyComposer = Objects.requireNonNull(strategyComposer, "strategyComposer must not be null");
        this.graphValidator = Objects.requireNonNull(graphValidator, "graphValidator must not be null");
        this.invariantSuite = Objects.requireNonNull(invariantSuite, "invariantSuite must not be null");
    }

    @Override
    public TranslationResult translate(TranslationRequest request) {
        return pipeline.run(request);
    }

    public <TOut, TCtx> TranslatorExportResult<TOut> translateAndExport(
        TranslationRequest request,
        TargetExporter<TOut, TCtx> exporter,
        TCtx context
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(exporter, "exporter must not be null");

        StrategyCompositionResult compositionResult = strategyComposer.composeResult(
            request,
            DecomposeOptions.defaults(),
            TranslateOptions.defaults(),
            MergeOptions.conservative()
        );

        List<String> graphDiagnostics = new ArrayList<>();
        var validationResult = graphValidator.validate(compositionResult.graph());
        if (validationResult.diagnostics() != null) {
            graphDiagnostics.addAll(validationResult.diagnostics().stream()
                .map(diagnostic -> diagnostic.code() + ": " + diagnostic.message())
                .toList());
        }
        graphDiagnostics.addAll(invariantSuite.check(compositionResult.graph()));

        TranslationResult translationResult = pipeline.run(request);
        List<String> mergedDiagnostics = new ArrayList<>();
        if (translationResult.diagnostics() != null) {
            mergedDiagnostics.addAll(translationResult.diagnostics());
        }
        mergedDiagnostics.addAll(graphDiagnostics);

        ExportInput input = new ExportInput(
            compositionResult.graph(),
            List.copyOf(mergedDiagnostics),
            new ExportInput.SourceInfo(buildSourceText(request), List.of())
        );
        TOut exported = exporter.export(input, context);
        return new TranslatorExportResult<>(
            translationResult,
            compositionResult.graph(),
            compositionResult.executionPlan(),
            List.copyOf(graphDiagnostics),
            exported
        );
    }

    private String buildSourceText(TranslationRequest request) {
        if (request.messages() == null || request.messages().isEmpty()) {
            return "";
        }
        return request.messages().stream()
            .map(message -> message.role() + ": " + message.content())
            .collect(Collectors.joining("\n"));
    }
}
