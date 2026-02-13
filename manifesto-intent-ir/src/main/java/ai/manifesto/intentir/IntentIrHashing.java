package ai.manifesto.intentir;

import ai.manifesto.core.utils.HashUtils;

import java.util.Objects;

/**
 * KR: canonical Intent IR에 대해 안정적인 SHA-256 해시를 생성하는 유틸리티입니다.
 * EN: Utility for generating stable SHA-256 hashes over canonical Intent IR payloads.
 */
public final class IntentIrHashing {
    private final IntentIrCanonicalizer canonicalizer;

    public IntentIrHashing() {
        this(new IntentIrCanonicalizer());
    }

    public IntentIrHashing(IntentIrCanonicalizer canonicalizer) {
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer must not be null");
    }

    public String hash(IntentIrDocument source) {
        String canonicalJson = canonicalizer.toCanonicalJson(source);
        return HashUtils.sha256(canonicalJson);
    }
}
