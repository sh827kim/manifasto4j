package ai.manifesto.sdk;

/**
 * KR: SDK memory 검증 결과입니다.
 * EN: SDK memory verification result.
 */
public record MemoryVerificationResult(
    boolean accepted,
    boolean freezeContext,
    String reason
) {
    public static MemoryVerificationResult accept() {
        return new MemoryVerificationResult(true, false, null);
    }

    public static MemoryVerificationResult reject(String reason) {
        return new MemoryVerificationResult(false, false, reason);
    }

    public static MemoryVerificationResult freeze(String reason) {
        return new MemoryVerificationResult(false, true, reason);
    }
}
