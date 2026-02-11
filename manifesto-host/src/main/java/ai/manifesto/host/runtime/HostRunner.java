package ai.manifesto.host.runtime;

import java.util.Objects;

/**
 * KR: 하나의 mailbox를 단일 runner 규칙으로 drain하는 동기 실행기입니다.
 * EN: Synchronous executor that drains one mailbox with single-runner semantics.
 */
public final class HostRunner {
    private final HostMailbox mailbox;
    private final HostJobHandler jobHandler;
    private final HostRunnerState state;

    public HostRunner(HostMailbox mailbox, HostJobHandler jobHandler) {
        this.mailbox = Objects.requireNonNull(mailbox, "mailbox is required");
        this.jobHandler = Objects.requireNonNull(jobHandler, "jobHandler is required");
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
        }
    }

    public void runUntilIdle() throws Exception {
        while (true) {
            if (state.isActive()) {
                state.requestKick();
                return;
            }
            state.markActive();
            try {
                HostJob job;
                while ((job = mailbox.dequeue()) != null) {
                    jobHandler.handle(job, mailbox);
                }
            } finally {
                state.markInactive();
            }
            boolean kickRequested = state.consumeKickRequested();
            if (mailbox.isEmpty() && !kickRequested) {
                return;
            }
        }
    }
}
