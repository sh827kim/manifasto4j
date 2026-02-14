package ai.manifesto.host;

/**
 * KR: effect 핸들러 실행 컨텍스트입니다.
 * EN: Execution context delivered to effect handlers.
 */
public record EffectExecutionContext(
    String executionKey,
    String intentId,
    String requirementId,
    String requirementType,
    int computeIteration,
    int attempt
) {
    public static EffectExecutionContext unknown() {
        return new EffectExecutionContext("unknown", "unknown", "unknown", "unknown", -1, -1);
    }
}
