package ai.manifesto.core.evaluator;

/**
 * FlowStatus - Flow의 실행 상태
 *
 * Flow가 평가되는 과정에서 다음 상태들을 거친다:
 * - RUNNING: 실행 중
 * - COMPLETE: 정상 완료
 * - PENDING: Effect 대기중 (Host의 처리 대기)
 * - HALTED: Halt로 명시적 중단
 * - ERROR: 에러 발생
 *
 * 상태는 항상 전이 방향이 있다:
 * RUNNING -> (COMPLETE | PENDING | HALTED | ERROR)
 *
 * 재진입 안전성: RUNNING이 아닌 상태에서는 더 이상 평가하지 않는다.
 */
public enum FlowStatus {
    RUNNING("running"),
    COMPLETE("complete"),
    PENDING("pending"),
    HALTED("halted"),
    ERROR("error");

    private final String code;

    FlowStatus(String code) {
        this.code = code;
    }

    /**
     * 상태 코드 반환 (직렬화용)
     */
    public String getCode() {
        return code;
    }

    /**
     * 코드로부터 상태 조회
     */
    public static FlowStatus fromCode(String code) {
        for (FlowStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown flow status: " + code);
    }

    /**
     * 상태 확인 헬퍼 메서드들
     */
    public boolean isRunning() {
        return this == RUNNING;
    }

    public boolean isComplete() {
        return this == COMPLETE;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isHalted() {
        return this == HALTED;
    }

    public boolean isError() {
        return this == ERROR;
    }

    /**
     * 종료 상태인지 확인 (RUNNING이 아닌 상태)
     */
    public boolean isTerminated() {
        return this != RUNNING;
    }
}
