package ai.manifesto.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SystemState - 시스템 상태
 * 애플리케이션의 내부 상태를 나타낸다.
 *
 * 필드:
 * - status: 현재 상태 (idle, computing, pending, error)
 * - lastError: 마지막 에러
 * - errors: 모든 에러 기록 (감시 추적용)
 * - pendingRequirements: Host가 처리해야 할 효과 목록
 * - currentAction: 현재 처리 중인 액션 (pending 상태일 때만)
 */
public class SystemState {

    public enum Status {
        IDLE,        // 유휴 상태
        COMPUTING,   // 계산 중
        PENDING,     // 효과 대기 중
        ERROR        // 에러 발생
    }

    private final Status status;
    private final ErrorValue lastError;
    private final List<ErrorValue> errors;
    private final List<Requirement> pendingRequirements;
    private final String currentAction;

    /**
     * 생성자
     */
    private SystemState(Status status, ErrorValue lastError,
                        List<ErrorValue> errors, List<Requirement> pendingRequirements,
                        String currentAction) {
        this.status = status;
        this.lastError = lastError;
        this.errors = new ArrayList<>(errors != null ? errors : new ArrayList<>());
        this.pendingRequirements = new ArrayList<>(pendingRequirements != null ?
            pendingRequirements : new ArrayList<>());
        this.currentAction = currentAction;
    }

    /**
     * 초기 시스템 상태 생성
     */
    public static SystemState initial() {
        return new SystemState(Status.IDLE, null, new ArrayList<>(),
            new ArrayList<>(), null);
    }

    public static SystemState of(Status status, ErrorValue lastError,
                                 List<ErrorValue> errors,
                                 List<Requirement> pendingRequirements,
                                 String currentAction) {
        return new SystemState(status, lastError, errors, pendingRequirements, currentAction);
    }

    // Getters
    public Status getStatus() { return status; }
    public ErrorValue getLastError() { return lastError; }
    public List<ErrorValue> getErrors() { return new ArrayList<>(errors); }
    public List<Requirement> getPendingRequirements() {
        return new ArrayList<>(pendingRequirements);
    }
    public String getCurrentAction() { return currentAction; }

    /**
     * Copy-on-Write 패턴: 새로운 상태를 생성한다
     */
    public SystemState withStatus(Status newStatus) {
        if (newStatus == this.status) return this;
        return new SystemState(newStatus, lastError, errors, pendingRequirements,
            currentAction);
    }

    public SystemState withLastError(ErrorValue error) {
        if (Objects.equals(error, this.lastError)) return this;
        return new SystemState(status, error, errors, pendingRequirements,
            currentAction);
    }

    public SystemState withError(ErrorValue error) {
        // 에러를 기록하고 lastError 업데이트
        List<ErrorValue> newErrors = new ArrayList<>(errors);
        newErrors.add(error);
        return new SystemState(status, error, newErrors, pendingRequirements,
            currentAction);
    }

    public SystemState withPendingRequirements(List<Requirement> reqs) {
        return new SystemState(status, lastError, errors, reqs, currentAction);
    }

    public SystemState addPendingRequirement(Requirement req) {
        List<Requirement> newReqs = new ArrayList<>(pendingRequirements);
        newReqs.add(req);
        return new SystemState(status, lastError, errors, newReqs, currentAction);
    }

    public SystemState withCurrentAction(String action) {
        if (Objects.equals(action, this.currentAction)) return this;
        return new SystemState(status, lastError, errors, pendingRequirements,
            action);
    }

    @Override
    public String toString() {
        return "SystemState{" +
               "status=" + status +
               ", lastError=" + lastError +
               ", errorCount=" + errors.size() +
               ", pendingRequirementCount=" + pendingRequirements.size() +
               ", currentAction='" + currentAction + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemState that)) return false;
        return status == that.status &&
               Objects.equals(lastError, that.lastError) &&
               Objects.equals(errors, that.errors) &&
               Objects.equals(pendingRequirements, that.pendingRequirements) &&
               Objects.equals(currentAction, that.currentAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, lastError, errors, pendingRequirements,
            currentAction);
    }
}
