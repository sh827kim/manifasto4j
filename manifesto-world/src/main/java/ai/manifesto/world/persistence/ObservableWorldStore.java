package ai.manifesto.world.persistence;

/**
 * KR: ObservableWorldStore는 WorldStore에 이벤트 구독 계약을 추가한 확장 인터페이스입니다.
 * EN: ObservableWorldStore extends WorldStore with event subscription contract.
 */
public interface ObservableWorldStore extends WorldStore {
    Runnable subscribe(StoreEventType type, StoreEventListener listener);

    Runnable subscribeAll(StoreEventListener listener);
}
