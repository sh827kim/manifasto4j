package ai.manifesto.host.runtime;

import java.util.Objects;

/**
 * KR: HostRunner는 큐/메일박스 작업을 단일 실행 규칙으로 처리하는 실행기입니다.
 * EN: HostRunner is a runner that processes queued jobs under single-runner semantics.
 */
public final class HostRunner {
    private final HostMailbox mailbox;
    private final HostJobHandler jobHandler;
    private final HostRunnerState state;
    private final HostRuntimeTraceSink traceSink;

    public HostRunner(HostMailbox mailbox, HostJobHandler jobHandler) {
        this(mailbox, jobHandler, HostRuntimeTraceSink.NOOP);
    }

    public HostRunner(HostMailbox mailbox, HostJobHandler jobHandler, HostRuntimeTraceSink traceSink) {
        this.mailbox = Objects.requireNonNull(mailbox, "mailbox is required");
        this.jobHandler = Objects.requireNonNull(jobHandler, "jobHandler is required");
        this.traceSink = traceSink != null ? traceSink : HostRuntimeTraceSink.NOOP;
        this.state = new HostRunnerState();
    }

    public HostRunnerState getState() {
        return state;
    }

    public void enqueue(HostJob job) {
        boolean wasEmpty = mailbox.isEmpty();
        mailbox.enqueue(job);
        if (wasEmpty) {
            state.requestKick();
            traceSink.onEvent(new HostRuntimeTraceEvent(
                "runner:kick",
                mailbox.getKey().value(),
                null,
                null,
                null,
                System.currentTimeMillis()
            ));
        }
    }

    public void runUntilIdle() throws Exception {
        while (true) {
            if (state.isActive()) {
                state.requestKick();
                return;
            }
            state.markActive();
            traceSink.onEvent(new HostRuntimeTraceEvent(
                "runner:start",
                mailbox.getKey().value(),
                null,
                null,
                null,
                System.currentTimeMillis()
            ));
            try {
                HostJob job;
                while ((job = mailbox.dequeue()) != null) {
                    traceSink.onEvent(new HostRuntimeTraceEvent(
                        "job:start",
                        mailbox.getKey().value(),
                        job.getType().name(),
                        null,
                        null,
                        null
                    ));
                    jobHandler.handle(job, mailbox);
                    traceSink.onEvent(new HostRuntimeTraceEvent(
                        "job:end",
                        mailbox.getKey().value(),
                        job.getType().name(),
                        null,
                        null,
                        null
                    ));
                }
            } finally {
                state.markInactive();
            }
            boolean kickRequested = state.consumeKickRequested();
            boolean queueEmpty = mailbox.isEmpty();
            traceSink.onEvent(new HostRuntimeTraceEvent(
                "runner:recheck",
                mailbox.getKey().value(),
                null,
                queueEmpty,
                kickRequested,
                null
            ));
            traceSink.onEvent(new HostRuntimeTraceEvent(
                "runner:end",
                mailbox.getKey().value(),
                null,
                null,
                null,
                System.currentTimeMillis()
            ));
            if (queueEmpty && !kickRequested) {
                return;
            }
        }
    }
}
