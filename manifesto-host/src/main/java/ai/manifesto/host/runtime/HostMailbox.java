package ai.manifesto.host.runtime;

/**
 * KR: HostMailbox는 Host 런타임 계층에서 host mailbox 계약을 정의하는 인터페이스입니다.
 * EN: HostMailbox is an interface defining the host mailbox contract in the Host runtime layer.
 */
public interface HostMailbox {
    ExecutionKey getKey();

    void enqueue(HostJob job);

    HostJob dequeue();

    HostJob peek();

    boolean isEmpty();

    int size();
}
