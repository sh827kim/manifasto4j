package ai.manifesto.host.runtime;

import ai.manifesto.core.Intent;

import java.util.Objects;

/**
 * KR: effect 반영 이후 동일 intent로 compute를 재진입하기 위한 job입니다.
 * EN: Job that re-enters compute with the same intent after effect application.
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
