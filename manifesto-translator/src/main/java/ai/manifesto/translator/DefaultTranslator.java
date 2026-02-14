package ai.manifesto.translator;

import ai.manifesto.intentir.DefaultIntentIrNormalizer;

import java.util.List;
import java.util.Objects;

/**
 * KR: interpret -> verify -> refine 단계를 순차 실행하는 기본 Translator 구현입니다.
 * EN: Default Translator implementation that runs interpret -> verify -> refine stages sequentially.
 */
public final class DefaultTranslator implements Translator {
    private final TranslatorPipeline pipeline;

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
        this.pipeline = new TranslatorPipeline(
            Objects.requireNonNull(interpreter, "interpreter must not be null"),
            Objects.requireNonNull(verifier, "verifier must not be null"),
            Objects.requireNonNull(refiner, "refiner must not be null"),
            plugins == null ? List.of() : plugins
        );
    }

    @Override
    public TranslationResult translate(TranslationRequest request) {
        return pipeline.run(request);
    }
}
