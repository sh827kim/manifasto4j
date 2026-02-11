package ai.manifesto.host.runtime;

/**
 * KR: HostRunnerState는 Host 런타임 계층에서 host runner state 역할을 수행하는 구현 타입입니다.
 * EN: HostRunnerState is an implementation type performing host runner state roles in the Host runtime layer.
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
