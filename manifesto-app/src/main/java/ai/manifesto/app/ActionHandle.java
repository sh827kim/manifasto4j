package ai.manifesto.app;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.ComputeStatus;

/**
 * KR: ActionHandle는 비동기 액션 실행의 완료 결과를 조회/구독하기 위한 핸들 객체입니다.
 * EN: ActionHandle is a handle object used to observe or await asynchronous action execution results.
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
