package ai.manifesto.translator;

/**
 * KR: interpret 결과를 스펙/정책 관점에서 검증하는 verify 단계 계약입니다.
 * EN: Verify-stage contract that validates interpreted drafts against spec/policy rules.
 */
public interface TranslatorVerifier {
    TranslationDraft verify(TranslationRequest request, TranslationDraft draft);
}
