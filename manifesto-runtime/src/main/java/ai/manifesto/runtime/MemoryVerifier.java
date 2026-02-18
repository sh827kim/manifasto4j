package ai.manifesto.runtime;

/**
 * KR: Memory ingest 시 key/value를 검증하는 계약입니다.
 * EN: Contract for validating memory ingest key/value.
 */
public interface MemoryVerifier {
    MemoryVerificationResult verify(String key, Object value);

    static MemoryVerifier allowAll() {
        return (key, value) -> MemoryVerificationResult.accept();
    }
}
