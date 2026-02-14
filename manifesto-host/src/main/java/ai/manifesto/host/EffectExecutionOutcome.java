package ai.manifesto.host;

/**
 * KR: effect 실행 결과(성공 patch 또는 실패 정보) 모델입니다.
 * EN: Outcome model of effect execution (success patches or failure info).
 */
public record EffectExecutionOutcome(
    EffectResult result,
    EffectExecutionError error
) {
    public boolean isSuccess() {
        return result != null && error == null;
    }

    public static EffectExecutionOutcome success(EffectResult result) {
        return new EffectExecutionOutcome(result, null);
    }

    public static EffectExecutionOutcome failure(EffectExecutionError error) {
        return new EffectExecutionOutcome(null, error);
    }
}
