package ai.manifesto.translator;

import ai.manifesto.intentir.IntentIrDocument;

import java.util.List;

/**
 * KR: Translator 실행 결과로 생성된 Intent IR과 진단 정보를 담는 모델입니다.
 * EN: Result model containing generated Intent IR and diagnostics from translator execution.
 */
public record TranslationResult(
    IntentIrDocument intentIr,
    List<String> diagnostics
) {}
