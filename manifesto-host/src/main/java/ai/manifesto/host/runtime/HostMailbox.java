package ai.manifesto.host.runtime;

/**
 * KR: 실행 키 단위 FIFO job 큐 계약입니다.
 * EN: FIFO job queue contract scoped by execution key.
 */
public interface HostMailbox {
    ExecutionKey getKey();

    void enqueue(HostJob job);

    HostJob dequeue();

    HostJob peek();

    boolean isEmpty();

    int size();
}
