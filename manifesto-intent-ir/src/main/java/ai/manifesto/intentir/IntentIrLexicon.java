package ai.manifesto.intentir;

/**
 * KR: Intent IR 문서가 도메인 어휘(허용 domain/action 집합)를 만족하는지 검증하는 계약입니다.
 * EN: Contract for validating whether an Intent IR document satisfies domain lexicon constraints.
 */
public interface IntentIrLexicon {
    IntentIrLexiconCheckResult check(IntentIrDocument document);
}
