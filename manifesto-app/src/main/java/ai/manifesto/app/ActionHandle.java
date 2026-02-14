package ai.manifesto.app;

import ai.manifesto.core.ComputeResult;
import ai.manifesto.core.ComputeStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: ActionHandle는 비동기 액션 실행의 완료 결과를 조회/구독하기 위한 핸들 객체입니다.
 * EN: ActionHandle is a handle object used to observe or await asynchronous action execution results.
 */
public final class ActionHandle {
    private final ComputeResult result;
    private final List<ActionUpdate> updates;

    public ActionHandle(ComputeResult result) {
        this(result, List.of());
    }

    public ActionHandle(ComputeResult result, List<ActionUpdate> updates) {
        this.result = result;
        this.updates = List.copyOf(updates == null ? List.of() : new ArrayList<>(updates));
    }

    public ComputeStatus getStatus() {
        return result.getStatus();
    }

    public ComputeResult getResult() {
        return result;
    }

    public List<ActionUpdate> getUpdates() {
        return updates;
    }

    public ActionPhase getPhase() {
        if (updates.isEmpty()) {
            return toPhase(getStatus());
        }
        ActionUpdate last = updates.get(updates.size() - 1);
        return last.phase();
    }

    private ActionPhase toPhase(ComputeStatus status) {
        if (status == null) {
            return ActionPhase.FAILED;
        }
        return switch (status) {
            case COMPLETE, HALTED -> ActionPhase.COMPLETED;
            case ERROR -> ActionPhase.FAILED;
            default -> ActionPhase.EXECUTING;
        };
    }
}
