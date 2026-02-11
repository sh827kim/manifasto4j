package ai.manifesto.host.runtime;

import ai.manifesto.core.Intent;

import java.util.Objects;

/**
 * KR: StartIntentJob는 Host 런타임 계층에서 start intent job 역할을 수행하는 구현 타입입니다.
 * EN: StartIntentJob is an implementation type performing start intent job roles in the Host runtime layer.
 */
public final class StartIntentJob implements HostJob {
    private final Intent intent;

    public StartIntentJob(Intent intent) {
        this.intent = Objects.requireNonNull(intent, "intent is required");
    }

    public Intent getIntent() {
        return intent;
    }

    @Override
    public HostJobType getType() {
        return HostJobType.START_INTENT;
    }
}
