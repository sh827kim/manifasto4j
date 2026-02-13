package ai.manifesto.intentir;

import ai.manifesto.core.utils.CanonicalUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: Intent IR 문서를 canonical JSON 문자열로 직렬화하는 유틸리티입니다.
 * EN: Utility for serializing Intent IR documents into canonical JSON strings.
 */
public final class IntentIrCanonicalizer {
    private final IntentIrNormalizer normalizer;

    public IntentIrCanonicalizer() {
        this(new DefaultIntentIrNormalizer());
    }

    public IntentIrCanonicalizer(IntentIrNormalizer normalizer) {
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer must not be null");
    }

    public String toCanonicalJson(IntentIrDocument source) {
        IntentIrDocument normalized = normalizer.normalize(source);
        return CanonicalUtils.toCanonical(toMapInternal(normalized));
    }

    public Map<String, Object> toMap(IntentIrDocument source) {
        IntentIrDocument normalized = normalizer.normalize(source);
        return toMapInternal(normalized);
    }

    private Map<String, Object> toMapInternal(IntentIrDocument normalized) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("schemaVersion", normalized.schemaVersion());
        out.put("domain", normalized.domain());
        out.put("action", normalized.action());
        out.put("input", normalized.input());
        out.put("meta", normalized.meta());
        return out;
    }
}
