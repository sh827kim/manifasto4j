package ai.manifesto.core.evaluator;

/**
 * KR: FlowStatus는 Core 평가 파이프라인에서 사용하는 flow status 분류 값을 열거합니다.
 * EN: FlowStatus enumerates flow status classification values used in the Core evaluation pipeline.
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
