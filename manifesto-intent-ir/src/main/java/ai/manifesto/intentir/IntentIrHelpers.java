package ai.manifesto.intentir;

import java.util.Map;
import java.util.Set;

/**
 * KR: intent-ir 기본 구현을 쉽게 생성하기 위한 helper 팩토리입니다.
 * EN: Helper factory for creating default intent-ir components.
 */
public final class IntentIrHelpers {
    private IntentIrHelpers() {
    }

    public static IntentIrCanonicalizer canonicalizer() {
        return new IntentIrCanonicalizer();
    }

    public static IntentIrKeyDeriver keyDeriver() {
        return new IntentIrKeyDeriver();
    }

    public static IntentIrLexicon lexicon(Map<String, Set<String>> allowedActionsByDomain) {
        return new DefaultIntentIrLexicon(allowedActionsByDomain);
    }

    public static IntentIrLexicon lexiconWithPolicies(Map<String, IntentIrLexiconPolicy> policiesByDomain) {
        return new DefaultIntentIrLexicon(policiesByDomain, true);
    }

    public static IntentIrResolver resolver(Map<String, Set<String>> allowedActionsByDomain) {
        return new DefaultIntentIrResolver(allowedActionsByDomain);
    }
}
