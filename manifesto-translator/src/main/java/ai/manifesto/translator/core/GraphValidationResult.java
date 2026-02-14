package ai.manifesto.translator.core;

import java.util.List;

/**
 * KR: intent graph 유효성 검증 결과입니다.
 * EN: Validation result for an intent graph.
 */
public record GraphValidationResult(
    boolean valid,
    List<TranslatorDiagnostic> diagnostics
) {}
