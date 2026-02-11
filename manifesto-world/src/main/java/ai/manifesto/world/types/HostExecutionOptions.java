package ai.manifesto.world.types;

import ai.manifesto.world.schema.IntentScope;

/**
 * KR: HostExecutionOptions는 실행 동작을 제어하는 옵션 값을 묶는 설정 객체입니다.
 * EN: HostExecutionOptions is a configuration object bundling options that control runtime behavior.
 */
public final class HostExecutionOptions {
    private final IntentScope approvedScope;

    public HostExecutionOptions(IntentScope approvedScope) {
        this.approvedScope = approvedScope;
    }

    public IntentScope getApprovedScope() {
        return approvedScope;
    }
}
