package ai.manifesto.intentir;

import java.util.List;

/**
 * KR: Resolver 처리 결과(보정된 문서 + 진단 정보)입니다.
 * EN: Resolver result containing normalized document and diagnostics.
 */
public record IntentIrResolveResult(
    IntentIrDocument document,
    List<String> diagnostics
) {}
