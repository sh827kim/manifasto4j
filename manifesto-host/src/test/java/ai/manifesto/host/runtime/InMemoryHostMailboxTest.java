package ai.manifesto.host.runtime;

import ai.manifesto.core.Intent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InMemoryHostMailbox 테스트")
class InMemoryHostMailboxTest {

    @Test
    @DisplayName("FIFO 순서로 job을 dequeue한다")
    void dequeuesJobsInFifoOrder() {
        ExecutionKey key = ExecutionKey.fromIntentId("intent-1");
        InMemoryHostMailbox mailbox = new InMemoryHostMailbox(key);

        StartIntentJob first = new StartIntentJob(new Intent("notify", Map.of(), "intent-1"));
        ContinueComputeJob second = new ContinueComputeJob(new Intent("notify", Map.of(), "intent-1"));
        mailbox.enqueue(first);
        mailbox.enqueue(second);

        assertEquals(2, mailbox.size());
        assertSame(first, mailbox.peek());
        assertSame(first, mailbox.dequeue());
        assertSame(second, mailbox.dequeue());
        assertTrue(mailbox.isEmpty());
        assertEquals(0, mailbox.size());
    }
}
