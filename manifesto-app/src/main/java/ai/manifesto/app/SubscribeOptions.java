package ai.manifesto.app;

/**
 * KR: SubscribeOptions는 구독 초기 emit/오류 격리 정책을 지정하는 옵션 계약입니다.
 * EN: SubscribeOptions defines initial emit and fault-isolation policy for subscriptions.
 */
public record SubscribeOptions(
    boolean emitInitial,
    boolean isolateHandlerErrors
) {
    public static SubscribeOptions defaults() {
        return new SubscribeOptions(true, true);
    }
}
