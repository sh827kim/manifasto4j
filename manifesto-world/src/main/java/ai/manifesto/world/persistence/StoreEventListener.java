package ai.manifesto.world.persistence;

/**
 * KR: WorldStore 이벤트를 수신하는 리스너 계약입니다.
 * EN: Listener contract for receiving WorldStore events.
 */
@FunctionalInterface
public interface StoreEventListener {
    void onEvent(StoreEvent event);
}
