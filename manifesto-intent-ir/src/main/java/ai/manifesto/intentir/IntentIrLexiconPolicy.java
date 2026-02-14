package ai.manifesto.intentir;

import java.util.Set;
import java.util.Map;

/**
 * KR: 도메인별 lexicon 정책(허용 action + 필수 입력 키 + 필수 메타 키)입니다.
 * EN: Domain lexicon policy (allowed actions + required input keys + required meta keys).
 */
public record IntentIrLexiconPolicy(
    Set<String> allowedActions,
    Set<String> requiredInputKeys,
    Set<String> requiredMetaKeys,
    Map<String, Set<String>> requiredRolesByAction,
    Map<String, Set<String>> selectionalRestrictionsByRole
) {
    public IntentIrLexiconPolicy(
        Set<String> allowedActions,
        Set<String> requiredInputKeys,
        Set<String> requiredMetaKeys
    ) {
        this(
            allowedActions,
            requiredInputKeys,
            requiredMetaKeys,
            Map.of(),
            Map.of()
        );
    }
}
