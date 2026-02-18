package ai.manifesto.sdk;

/**
 * KR: SDK memory verifier 계약입니다.
 * EN: SDK memory verifier contract.
 */
public interface MemoryVerifier {
    MemoryVerificationResult verify(String key, Object value);

    static MemoryVerifier allowAll() {
        return (key, value) -> MemoryVerificationResult.accept();
    }
}
