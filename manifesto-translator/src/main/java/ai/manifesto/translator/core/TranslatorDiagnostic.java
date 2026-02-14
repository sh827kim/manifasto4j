package ai.manifesto.translator.core;

/**
 * KR: translator 단계에서 수집되는 정형 진단 정보입니다.
 * EN: Structured diagnostic information collected during translator phases.
 */
public record TranslatorDiagnostic(
    String code,
    DiagnosticLevel level,
    String message
) {}
