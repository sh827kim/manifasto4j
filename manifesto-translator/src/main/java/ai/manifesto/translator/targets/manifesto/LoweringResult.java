package ai.manifesto.translator.targets.manifesto;

import ai.manifesto.intentir.IntentIrLowerResult;

/**
 * KR: Manifesto lowering 결과 모델입니다.
 * EN: Result model for Manifesto lowering.
 */
public record LoweringResult(
    String status,
    IntentIrLowerResult intentBody,
    String reason,
    LoweringFailure failure
) {
    public static LoweringResult ready(IntentIrLowerResult intentBody) {
        return new LoweringResult("ready", intentBody, null, null);
    }

    public static LoweringResult deferred(String reason) {
        return new LoweringResult("deferred", null, reason, null);
    }

    public static LoweringResult failed(LoweringFailure failure) {
        return new LoweringResult("failed", null, null, failure);
    }

    public boolean isReady() {
        return "ready".equals(status);
    }
}
