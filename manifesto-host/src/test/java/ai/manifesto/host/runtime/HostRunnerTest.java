package ai.manifesto.host.runtime;

import ai.manifesto.core.Intent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HostRunner 테스트")
class HostRunnerTest {

    @Test
    @DisplayName("re-entrant 호출이 있어도 단일 runner로 mailbox를 끝까지 drain한다")
    void runUntilIdleHandlesReentrantCall() throws Exception {
        Intent intent = new Intent("notify", Map.of(), "intent-1");
        InMemoryHostMailbox mailbox = new InMemoryHostMailbox(ExecutionKey.fromIntentId(intent.getIntentId()));
        List<HostJobType> handledTypes = new ArrayList<>();
        AtomicReference<HostRunner> runnerRef = new AtomicReference<>();

        HostRunner runner = new HostRunner(mailbox, (job, ignoredMailbox) -> {
            handledTypes.add(job.getType());
            if (job.getType() == HostJobType.START_INTENT) {
                runnerRef.get().enqueue(new ContinueComputeJob(intent));
                runnerRef.get().runUntilIdle();
            }
        });
        runnerRef.set(runner);

        runner.enqueue(new StartIntentJob(intent));
        runner.runUntilIdle();

        assertEquals(List.of(HostJobType.START_INTENT, HostJobType.CONTINUE_COMPUTE), handledTypes);
        assertTrue(mailbox.isEmpty());
        assertFalse(runner.getState().isActive());
    }
}
