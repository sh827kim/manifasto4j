package ai.manifesto.translator;

import ai.manifesto.translator.core.ExecutionPlan;
import ai.manifesto.translator.core.IntentGraph;

import java.util.List;

/**
 * KR: translate + export 통합 실행 결과 모델입니다.
 * EN: Result model for integrated translate + export execution.
 */
public record TranslatorExportResult<TOut>(
    TranslationResult translationResult,
    IntentGraph graph,
    ExecutionPlan executionPlan,
    List<String> graphDiagnostics,
    TOut exported
) {}
