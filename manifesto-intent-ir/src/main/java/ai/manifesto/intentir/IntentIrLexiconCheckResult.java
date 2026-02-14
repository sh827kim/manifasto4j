package ai.manifesto.intentir;

import java.util.List;

/**
 * KR: Lexicon 검증 결과(유효성 + 진단 코드 목록)입니다.
 * EN: Lexicon validation result (valid flag + diagnostic code list).
 */
public record IntentIrLexiconCheckResult(
    boolean valid,
    List<String> diagnostics
) {}
