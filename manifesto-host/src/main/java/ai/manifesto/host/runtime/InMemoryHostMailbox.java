package ai.manifesto.host.runtime;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * KR: InMemoryHostMailbox는 메모리 기반 저장 전략을 사용하는 구현 클래스입니다.
 * EN: InMemoryHostMailbox is an implementation class that uses an in-memory storage strategy.
 */
public final class InMemoryHostMailbox implements HostMailbox {
    private final ExecutionKey key;
    private final Deque<HostJob> queue;

    public InMemoryHostMailbox(ExecutionKey key) {
        this.key = Objects.requireNonNull(key, "key is required");
        this.queue = new ArrayDeque<>();
    }

    @Override
    public ExecutionKey getKey() {
        return key;
    }

    @Override
    public void enqueue(HostJob job) {
        queue.addLast(Objects.requireNonNull(job, "job is required"));
    }

    @Override
    public HostJob dequeue() {
        return queue.pollFirst();
    }

    @Override
    public HostJob peek() {
        return queue.peekFirst();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return queue.size();
    }
}
