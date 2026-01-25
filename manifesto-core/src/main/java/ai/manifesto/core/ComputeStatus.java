package ai.manifesto.core;

/**
 * ComputeStatus - compute() 함수의 실행 결과 상태
 *
 * 계산이 어떻게 종료되었는지를 나타낸다:
 * - COMPLETE: Flow가 완료되었고 펼딩된 효과가 없음
 * - PENDING: Flow가 효과를 만났으므로 Host 대기 중
 * - HALTED: Flow가 명시적으로 중단됨 (정상)
 * - ERROR: 에러 발생 (비정상)
 *
 * Java 17+ 기능: enum은 sealed 클래스처럼 동작
 */
public enum ComputeStatus {
    COMPLETE("complete"),     // 흐름 완료, 펼딩 없음
    PENDING("pending"),        // 효과 대기 중 (Host 처리 필요)
    HALTED("halted"),         // 명시적 중단 (정상)
    ERROR("error");           // 에러 발생 (비정상)

    private final String code;

    ComputeStatus(String code) {
        this.code = code;
    }

    /**
     * 상태 코드 반환
     */
    public String getCode() {
        return code;
    }

    /**
     * 코드로부터 상태 추출
     *
     * @param code 상태 코드 (예: "complete", "pending")
     * @return 대응하는 ComputeStatus
     * @throws IllegalArgumentException 알 수 없는 코드
     */
    public static ComputeStatus fromCode(String code) {
        for (ComputeStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + code);
    }

    /**
     * 상태 확인 헬퍼
     */
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
}
