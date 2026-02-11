package ai.manifesto.host.runtime;

import ai.manifesto.core.Intent;

import java.util.Objects;

/**
 * KR: 새 intent 처리를 시작하기 위한 진입 job입니다.
 * EN: Entry job that starts processing a new intent.
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
