package ai.manifesto.app;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.ComputeStatus;

/**
 * ActionHandle - 액션 실행 결과를 관찰하기 위한 핸들
 */
public final class ActionHandle {
    private final ComputeResult result;

    public ActionHandle(ComputeResult result) {
        this.result = result;
    }

    public ComputeStatus getStatus() {
        return result.getStatus();
    }

    public ComputeResult getResult() {
        return result;
    }
}
