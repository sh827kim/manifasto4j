package ai.manifesto.translator;

import ai.manifesto.intentir.IntentIrDocument;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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

    public TranslatorPipeline(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner,
        List<TranslatorPipelinePlugin> plugins
    ) {
        this.interpreter = Objects.requireNonNull(interpreter, "interpreter must not be null");
        this.verifier = Objects.requireNonNull(verifier, "verifier must not be null");
        this.refiner = Objects.requireNonNull(refiner, "refiner must not be null");
        this.plugins = List.copyOf(plugins == null ? List.of() : plugins);
    }

    public TranslationResult run(TranslationRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        List<String> diagnostics = new ArrayList<>();

        for (TranslatorPipelinePlugin plugin : plugins) {
            plugin.beforeInterpret(request, diagnostics);
        }

        TranslationDraft interpreted = interpreter.interpret(request);
        if (interpreted.diagnostics() != null) {
            diagnostics.addAll(interpreted.diagnostics());
        }

        TranslationDraft interpretedWithPlugins = interpreted;
        for (TranslatorPipelinePlugin plugin : plugins) {
            interpretedWithPlugins = Objects.requireNonNull(
                plugin.afterInterpret(request, interpretedWithPlugins, diagnostics),
                "plugin afterInterpret must not return null: " + plugin.name()
            );
        }

        for (TranslatorPipelinePlugin plugin : plugins) {
            plugin.beforeVerify(request, interpretedWithPlugins, diagnostics);
        }

        TranslationDraft verified = verifier.verify(request, interpretedWithPlugins);
        if (verified.diagnostics() != null) {
            diagnostics.addAll(verified.diagnostics());
        }

        TranslationDraft verifiedWithPlugins = verified;
        for (TranslatorPipelinePlugin plugin : plugins) {
            verifiedWithPlugins = Objects.requireNonNull(
                plugin.afterVerify(request, verifiedWithPlugins, diagnostics),
                "plugin afterVerify must not return null: " + plugin.name()
            );
        }

        for (TranslatorPipelinePlugin plugin : plugins) {
            plugin.beforeRefine(request, verifiedWithPlugins, diagnostics);
        }

        IntentIrDocument intentIr = refiner.refine(request, verifiedWithPlugins);
        IntentIrDocument intentIrWithPlugins = intentIr;
        for (TranslatorPipelinePlugin plugin : plugins) {
            intentIrWithPlugins = Objects.requireNonNull(
                plugin.afterRefine(request, verifiedWithPlugins, intentIrWithPlugins, diagnostics),
                "plugin afterRefine must not return null: " + plugin.name()
            );
        }

        List<String> dedupedDiagnostics = new ArrayList<>(new LinkedHashSet<>(diagnostics));
        return new TranslationResult(intentIrWithPlugins, List.copyOf(dedupedDiagnostics));
    }
}
