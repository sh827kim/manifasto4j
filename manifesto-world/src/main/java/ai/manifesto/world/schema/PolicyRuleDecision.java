package ai.manifesto.world.schema;

/**
 * KR: PolicyRuleDecision는 World 스키마 계층에서 사용하는 policy rule decision 분류 값을 열거합니다.
 * EN: PolicyRuleDecision enumerates policy rule decision classification values used in the World schema layer.
 */
public enum PolicyRuleDecision {
    APPROVE,
    REJECT,
    ESCALATE
}
