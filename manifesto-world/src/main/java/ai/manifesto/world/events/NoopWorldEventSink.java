package ai.manifesto.world.events;

/**
 * KR: NoopWorldEventSink는 입력을 무시하고 동작을 생략하는 no-op 구현 클래스입니다.
 * EN: NoopWorldEventSink is a no-op implementation class that ignores input and performs no side effects.
 */
public final class NoopWorldEventSink implements WorldEventSink {
    @Override
    public void emit(WorldEvent event) {
        // no-op
    }
}
