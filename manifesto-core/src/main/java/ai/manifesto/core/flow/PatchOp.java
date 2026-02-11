package ai.manifesto.core.flow;

/**
 * KR: PatchOp는 Core 플로우 계층에서 사용하는 patch op 분류 값을 열거합니다.
 * EN: PatchOp enumerates patch op classification values used in the Core flow layer.
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
