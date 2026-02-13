package ai.manifesto.translator;

import ai.manifesto.intentir.IntentIrDocument;
import ai.manifesto.intentir.IntentIrNormalizer;

import java.util.Map;
import java.util.Objects;

/**
 * KR: 검증 초안을 Intent IR 정규 문서로 변환하는 기본 refine 구현입니다.
 * EN: Default refine implementation that converts verified drafts into normalized Intent IR documents.
 */
public final class DefaultTranslatorRefiner implements TranslatorRefiner {
    private final IntentIrNormalizer normalizer;

    public DefaultTranslatorRefiner(IntentIrNormalizer normalizer) {
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer must not be null");
    }

    @Override
    public IntentIrDocument refine(TranslationRequest request, TranslationDraft draft) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(draft, "draft must not be null");

        IntentIrDocument source = new IntentIrDocument(
            "1.0.0",
            draft.domainName(),
            draft.actionName(),
            draft.input() == null ? Map.of() : draft.input(),
            draft.meta() == null ? Map.of() : draft.meta()
        );
        return normalizer.normalize(source);
    }
}
