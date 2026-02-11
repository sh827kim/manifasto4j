package ai.manifesto.host.runtime;

/**
 * KR: HostJobHandler는 특정 도메인 이벤트/요청을 처리하는 핸들러 타입입니다.
 * EN: HostJobHandler is a handler type that processes specific domain events or requests.
 */
@FunctionalInterface
public interface HostJobHandler {
    void handle(HostJob job, HostMailbox mailbox) throws Exception;
}
