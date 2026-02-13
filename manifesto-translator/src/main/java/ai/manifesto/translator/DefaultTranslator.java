package ai.manifesto.translator;

import ai.manifesto.intentir.DefaultIntentIrNormalizer;

import java.util.List;
import java.util.Objects;

/**
 * KR: interpret -> verify -> refine 단계를 순차 실행하는 기본 Translator 구현입니다.
 * EN: Default Translator implementation that runs interpret -> verify -> refine stages sequentially.
 */
public final class DefaultTranslator implements Translator {
    private final TranslatorInterpreter interpreter;
    private final TranslatorVerifier verifier;
    private final TranslatorRefiner refiner;

    public DefaultTranslator() {
        this(
            new RuleBasedInterpreter(),
            new DefaultTranslatorVerifier(),
            new DefaultTranslatorRefiner(new DefaultIntentIrNormalizer())
        );
    }

    public DefaultTranslator(
        TranslatorInterpreter interpreter,
        TranslatorVerifier verifier,
        TranslatorRefiner refiner
    ) {
        this.interpreter = Objects.requireNonNull(interpreter, "interpreter must not be null");
        this.verifier = Objects.requireNonNull(verifier, "verifier must not be null");
        this.refiner = Objects.requireNonNull(refiner, "refiner must not be null");
    }

    @Override
    public TranslationResult translate(TranslationRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        TranslationDraft interpreted = interpreter.interpret(request);
        TranslationDraft verified = verifier.verify(request, interpreted);
        return new TranslationResult(
            refiner.refine(request, verified),
            verified.diagnostics() == null ? List.of() : verified.diagnostics()
        );
    }
}
