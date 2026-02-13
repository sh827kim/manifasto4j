package ai.manifesto.translator;

import ai.manifesto.intentir.IntentIrDocument;

/**
 * KR: 검증된 초안을 최종 Intent IR 문서로 정제하는 refine 단계 계약입니다.
 * EN: Refine-stage contract that converts verified drafts into final Intent IR documents.
 */
public interface TranslatorRefiner {
    IntentIrDocument refine(TranslationRequest request, TranslationDraft draft);
}
