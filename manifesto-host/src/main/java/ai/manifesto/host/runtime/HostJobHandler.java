package ai.manifesto.host.runtime;

/**
 * KR: runner가 dequeue한 job을 처리하는 실행 함수 계약입니다.
 * EN: Execution function contract that handles jobs dequeued by the runner.
 */
@FunctionalInterface
public interface HostJobHandler {
    void handle(HostJob job, HostMailbox mailbox) throws Exception;
}
