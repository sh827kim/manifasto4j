package ai.manifesto.translator;

import ai.manifesto.intentir.IntentIrDocument;
import ai.manifesto.translator.pipeline.TranslatorDiagnosticsBag;
import ai.manifesto.translator.pipeline.TranslatorPipelineOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * KR: interpret -> verify -> refine 3단계를 실행하고 plugin hook을 적용하는 파이프라인 실행기입니다.
 * EN: Pipeline executor that runs interpret -> verify -> refine stages and applies plugin hooks.
 */
public final class TranslatorPipeline {
    private final TranslatorInterpreter interpreter;
    private final TranslatorVerifier verifier;
    private final TranslatorRefiner refiner;
    private final List<TranslatorPipelinePlugin> plugins;
    private final TranslatorPipelineOptions options;

    public TranslatorPipeline(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner,
        List<TranslatorPipelinePlugin> plugins
    ) {
        this(interpreter, verifier, refiner, plugins, TranslatorPipelineOptions.defaults());
    }

    public TranslatorPipeline(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner,
        List<TranslatorPipelinePlugin> plugins,
        TranslatorPipelineOptions options
    ) {
        this.interpreter = Objects.requireNonNull(interpreter, "interpreter must not be null");
        this.verifier = Objects.requireNonNull(verifier, "verifier must not be null");
        this.refiner = Objects.requireNonNull(refiner, "refiner must not be null");
        List<TranslatorPipelinePlugin> pluginList = new ArrayList<>(plugins == null ? List.of() : plugins);
        TranslatorPipelineOptions safeOptions = options == null ? TranslatorPipelineOptions.defaults() : options;
        if (safeOptions.sortPluginsByPriority()) {
            pluginList.sort(Comparator.comparingInt(TranslatorPipelinePlugin::priority).reversed());
        }
        this.plugins = List.copyOf(pluginList);
        this.options = safeOptions;
    }

    public TranslationResult run(TranslationRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        TranslatorDiagnosticsBag diagnostics = new TranslatorDiagnosticsBag(options.diagnosticsPolicy());

        for (TranslatorPipelinePlugin plugin : plugins) {
            List<String> hookDiagnostics = new ArrayList<>(diagnostics.toList());
            plugin.beforeInterpret(request, hookDiagnostics);
            diagnostics.addAll(hookDiagnostics);
        }

        TranslationDraft interpreted = interpreter.interpret(request);
        if (interpreted.diagnostics() != null) {
            diagnostics.addAll(interpreted.diagnostics());
        }

        TranslationDraft interpretedWithPlugins = interpreted;
        for (TranslatorPipelinePlugin plugin : plugins) {
            List<String> hookDiagnostics = new ArrayList<>(diagnostics.toList());
            interpretedWithPlugins = Objects.requireNonNull(
                plugin.afterInterpret(request, interpretedWithPlugins, hookDiagnostics),
                "plugin afterInterpret must not return null: " + plugin.name()
            );
            diagnostics.addAll(hookDiagnostics);
            diagnostics.addAll(interpretedWithPlugins.diagnostics());
        }

        for (TranslatorPipelinePlugin plugin : plugins) {
            List<String> hookDiagnostics = new ArrayList<>(diagnostics.toList());
            plugin.beforeVerify(request, interpretedWithPlugins, hookDiagnostics);
            diagnostics.addAll(hookDiagnostics);
        }

        TranslationDraft verified = verifier.verify(request, interpretedWithPlugins);
        if (verified.diagnostics() != null) {
            diagnostics.addAll(verified.diagnostics());
        }

        TranslationDraft verifiedWithPlugins = verified;
        for (TranslatorPipelinePlugin plugin : plugins) {
            List<String> hookDiagnostics = new ArrayList<>(diagnostics.toList());
            verifiedWithPlugins = Objects.requireNonNull(
                plugin.afterVerify(request, verifiedWithPlugins, hookDiagnostics),
                "plugin afterVerify must not return null: " + plugin.name()
            );
            diagnostics.addAll(hookDiagnostics);
            diagnostics.addAll(verifiedWithPlugins.diagnostics());
        }

        for (TranslatorPipelinePlugin plugin : plugins) {
            List<String> hookDiagnostics = new ArrayList<>(diagnostics.toList());
            plugin.beforeRefine(request, verifiedWithPlugins, hookDiagnostics);
            diagnostics.addAll(hookDiagnostics);
        }

        IntentIrDocument intentIr = refiner.refine(request, verifiedWithPlugins);
        IntentIrDocument intentIrWithPlugins = intentIr;
        for (TranslatorPipelinePlugin plugin : plugins) {
            List<String> hookDiagnostics = new ArrayList<>(diagnostics.toList());
            intentIrWithPlugins = Objects.requireNonNull(
                plugin.afterRefine(request, verifiedWithPlugins, intentIrWithPlugins, hookDiagnostics),
                "plugin afterRefine must not return null: " + plugin.name()
            );
            diagnostics.addAll(hookDiagnostics);
        }

        return new TranslationResult(intentIrWithPlugins, diagnostics.toList());
    }
}
