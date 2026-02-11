package ai.manifesto.world.events;

/**
 * KR: WorldEventSink는 World 이벤트 계층에서 world event sink 계약을 정의하는 인터페이스입니다.
 * EN: WorldEventSink is an interface defining the world event sink contract in the World event layer.
 */
public interface WorldEventSink {
    void emit(WorldEvent event);
}
