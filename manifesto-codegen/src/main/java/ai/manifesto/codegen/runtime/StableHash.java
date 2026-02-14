package ai.manifesto.codegen.runtime;

import ai.manifesto.core.utils.CanonicalUtils;
import ai.manifesto.core.utils.HashUtils;

/**
 * KR: canonical 표현 기반 안정 해시 유틸입니다.
 * EN: Stable hash utility based on canonical representation.
 */
public final class StableHash {
    private StableHash() {
    }

    public static String stableHash(Object input) {
        String canonical = CanonicalUtils.toCanonical(input);
        return HashUtils.sha256Sync(canonical);
    }
}
