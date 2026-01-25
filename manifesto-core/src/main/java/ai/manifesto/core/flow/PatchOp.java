package ai.manifesto.core.flow;

/**
 * PatchOp - 상태 변경의 세 가지 연산
 *
 * Manifesto는 상태 변경을 정확히 세 가지 연산으로만 지원한다:
 * 1. SET - 경로의 값을 설정 (없으면 생성)
 * 2. UNSET - 경로의 속성을 제거
 * 3. MERGE - 경로의 객체에 얕은 병합 수행
 *
 * 이 세 연산으로 모든 상태 변경을 표현할 수 있다.
 */
public enum PatchOp {
    SET("set"),
    UNSET("unset"),
    MERGE("merge");

    private final String code;

    PatchOp(String code) {
        this.code = code;
    }

    /**
     * 연산 코드 반환 (직렬화용)
     */
    public String getCode() {
        return code;
    }

    /**
     * 코드로부터 연산 조회
     */
    public static PatchOp fromCode(String code) {
        for (PatchOp op : values()) {
            if (op.code.equals(code)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown patch operation: " + code);
    }

    /**
     * SET 연산 확인
     */
    public boolean isSet() {
        return this == SET;
    }

    /**
     * UNSET 연산 확인
     */
    public boolean isUnset() {
        return this == UNSET;
    }

    /**
     * MERGE 연산 확인
     */
    public boolean isMerge() {
        return this == MERGE;
    }
}
