package ai.manifesto.translator.targets.manifesto;

/**
 * KR: Manifesto lowering 실패 상세 정보입니다.
 * EN: Detailed failure information for Manifesto lowering.
 */
public record LoweringFailure(
    LoweringFailureKind kind,
    String details
) {}
