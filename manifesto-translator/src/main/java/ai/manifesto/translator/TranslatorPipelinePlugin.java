package ai.manifesto.translator;

import ai.manifesto.intentir.IntentIrDocument;

import java.util.List;

/**
 * KR: Translator 파이프라인의 stage 사이에서 진단 추가/결과 보정을 수행하는 플러그인 계약입니다.
 * EN: Plugin contract for adding diagnostics and refining outputs between translator pipeline stages.
 */
public interface TranslatorPipelinePlugin {
    default String name() {
        return getClass().getSimpleName();
    }

    default int priority() {
        return 0;
    }

    default TranslatorPluginType type() {
        return TranslatorPluginType.TRANSFORMER;
    }

    default void beforeInterpret(TranslationRequest request, List<String> diagnostics) {
    }

    default TranslationDraft afterInterpret(
        TranslationRequest request,
        TranslationDraft draft,
        List<String> diagnostics
    ) {
        return draft;
    }

    default void beforeVerify(TranslationRequest request, TranslationDraft draft, List<String> diagnostics) {
    }

    default TranslationDraft afterVerify(
        TranslationRequest request,
        TranslationDraft verifiedDraft,
        List<String> diagnostics
    ) {
        return verifiedDraft;
    }

    default void beforeRefine(TranslationRequest request, TranslationDraft draft, List<String> diagnostics) {
    }

    default IntentIrDocument afterRefine(
        TranslationRequest request,
        TranslationDraft draft,
        IntentIrDocument intentIr,
        List<String> diagnostics
    ) {
        return intentIr;
    }
}
