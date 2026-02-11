package ai.manifesto.host.runtime;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.Intent;

import java.util.Objects;

/**
 * KR: PENDING 결과의 requirement를 실행하고 patch를 반영하기 위한 job입니다.
 * EN: Job that fulfills requirements from a pending result and applies resulting patches.
 */
public final class FulfillRequirementsJob implements HostJob {
    private final ComputeResult pendingResult;
    private final Intent intent;

    public FulfillRequirementsJob(ComputeResult pendingResult, Intent intent) {
        this.pendingResult = Objects.requireNonNull(pendingResult, "pendingResult is required");
        this.intent = Objects.requireNonNull(intent, "intent is required");
    }

    public ComputeResult getPendingResult() {
        return pendingResult;
    }

    public Intent getIntent() {
        return intent;
    }

    @Override
    public HostJobType getType() {
        return HostJobType.FULFILL_REQUIREMENTS;
    }
}
