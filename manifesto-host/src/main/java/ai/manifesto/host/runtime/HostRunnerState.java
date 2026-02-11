package ai.manifesto.host.runtime;

/**
 * KR: 단일 runner 보장과 lost-wakeup 방지를 위한 coordination 상태입니다.
 * EN: Coordination state for single-runner guarantees and lost-wakeup prevention.
 */
public final class HostRunnerState {
    private boolean active;
    private boolean kickRequested;

    public boolean isActive() {
        return active;
    }

    public void markActive() {
        this.active = true;
    }

    public void markInactive() {
        this.active = false;
    }

    public void requestKick() {
        this.kickRequested = true;
    }

    public boolean consumeKickRequested() {
        boolean value = kickRequested;
        kickRequested = false;
        return value;
    }
}
