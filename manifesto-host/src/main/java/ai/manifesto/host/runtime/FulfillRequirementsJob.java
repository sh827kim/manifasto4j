package ai.manifesto.host.runtime;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.Intent;

import java.util.Objects;

/**
 * KR: FulfillRequirementsJob는 Host 런타임 계층에서 fulfill requirements job 역할을 수행하는 구현 타입입니다.
 * EN: FulfillRequirementsJob is an implementation type performing fulfill requirements job roles in the Host runtime layer.
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
