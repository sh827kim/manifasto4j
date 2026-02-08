package ai.manifesto.world.types;

import ai.manifesto.world.schema.IntentScope;

public final class HostExecutionOptions {
    private final IntentScope approvedScope;

    public HostExecutionOptions(IntentScope approvedScope) {
        this.approvedScope = approvedScope;
    }

    public IntentScope getApprovedScope() {
        return approvedScope;
    }
}
