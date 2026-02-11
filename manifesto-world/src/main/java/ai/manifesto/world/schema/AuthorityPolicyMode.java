package ai.manifesto.world.schema;

/**
 * KR: AuthorityPolicyMode는 World 도메인 상태/분류 체계를 정의하는 열거형입니다.
 * EN: AuthorityPolicyMode is an enum defining World-domain status or classification categories.
 */
public enum AuthorityPolicyMode {
    AUTO_APPROVE,
    HITL,
    POLICY_RULES,
    TRIBUNAL
}
