package ai.manifesto.host.runtime;

import ai.manifesto.core.Intent;

import java.util.Objects;

/**
 * KR: ContinueComputeJob는 Host 런타임 계층에서 continue compute job 역할을 수행하는 구현 타입입니다.
 * EN: ContinueComputeJob is an implementation type performing continue compute job roles in the Host runtime layer.
 */
public final class ContinueComputeJob implements HostJob {
    private final Intent intent;

    public ContinueComputeJob(Intent intent) {
        this.intent = Objects.requireNonNull(intent, "intent is required");
    }

    public Intent getIntent() {
        return intent;
    }

    @Override
    public HostJobType getType() {
        return HostJobType.CONTINUE_COMPUTE;
    }
}
