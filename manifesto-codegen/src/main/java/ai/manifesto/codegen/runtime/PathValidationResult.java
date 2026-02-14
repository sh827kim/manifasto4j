package ai.manifesto.codegen.runtime;

/**
 * KR: 경로 검증/정규화 결과 모델입니다.
 * EN: Result model for path validation/normalization.
 */
public record PathValidationResult(
    boolean valid,
    String normalized,
    String reason
) {
    public static PathValidationResult success(String normalized) {
        return new PathValidationResult(true, normalized, null);
    }

    public static PathValidationResult failure(String reason) {
        return new PathValidationResult(false, null, reason);
    }
}
