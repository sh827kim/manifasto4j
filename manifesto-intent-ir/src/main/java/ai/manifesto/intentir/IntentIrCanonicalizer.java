package ai.manifesto.intentir;

import ai.manifesto.core.utils.CanonicalUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KR: Intent IR 문서를 canonical JSON 문자열로 직렬화하는 유틸리티입니다.
 * EN: Utility for serializing Intent IR documents into canonical JSON strings.
 */
public final class IntentIrCanonicalizer {
    private static final Set<String> VOLATILE_META_KEYS = Set.of(
        "requestId",
        "traceId",
        "timestamp",
        "sessionId",
        "sourceEventId"
    );

    private final IntentIrNormalizer normalizer;

    public IntentIrCanonicalizer() {
        this(new DefaultIntentIrNormalizer());
    }

    public IntentIrCanonicalizer(IntentIrNormalizer normalizer) {
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer must not be null");
    }

    public String toCanonicalJson(IntentIrDocument source) {
        return toStrictCanonicalJson(source);
    }

    public String toStrictCanonicalJson(IntentIrDocument source) {
        return CanonicalUtils.toCanonical(toMapInternal(canonicalizeStrict(source)));
    }

    public String toSemanticCanonicalJson(IntentIrDocument source) {
        return CanonicalUtils.toCanonical(toMapInternal(canonicalizeSemantic(source)));
    }

    public Map<String, Object> toMap(IntentIrDocument source) {
        return toStrictMap(source);
    }

    public Map<String, Object> toStrictMap(IntentIrDocument source) {
        return toMapInternal(canonicalizeStrict(source));
    }

    public Map<String, Object> toSemanticMap(IntentIrDocument source) {
        return toMapInternal(canonicalizeSemantic(source));
    }

    public IntentIrDocument canonicalizeStrict(IntentIrDocument source) {
        return normalizer.normalize(source);
    }

    public IntentIrDocument canonicalizeSemantic(IntentIrDocument source) {
        IntentIrDocument strict = canonicalizeStrict(source);
        Map<String, Object> semanticMeta = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : strict.meta().entrySet()) {
            if (!VOLATILE_META_KEYS.contains(entry.getKey())) {
                semanticMeta.put(entry.getKey(), entry.getValue());
            }
        }
        return new IntentIrDocument(
            strict.schemaVersion(),
            strict.domain(),
            strict.action(),
            strict.input(),
            semanticMeta
        );
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
