package ai.manifesto.intentir;

import ai.manifesto.core.utils.HashUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KR: Intent IR 문서에서 strict/semantic/sim 키를 파생하는 유틸리티입니다.
 * EN: Utility for deriving strict/semantic/sim keys from an Intent IR document.
 */
public final class IntentIrKeyDeriver {
    private static final Set<String> VOLATILE_META_KEYS = Set.of(
        "requestId",
        "traceId",
        "timestamp",
        "sessionId",
        "sourceEventId"
    );

    private final IntentIrCanonicalizer canonicalizer;

    public IntentIrKeyDeriver() {
        this(new IntentIrCanonicalizer());
    }

    public IntentIrKeyDeriver(IntentIrCanonicalizer canonicalizer) {
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer must not be null");
    }

    public String deriveStrictKey(IntentIrDocument source) {
        IntentIrDocument normalized = normalizeForKey(source);
        String canonical = canonicalizer.toCanonicalJson(normalized);
        return HashUtils.sha256("strict:" + canonical);
    }

    public String deriveSemanticKey(IntentIrDocument source) {
        IntentIrDocument semantic = toSemanticProjection(normalizeForKey(source));
        String canonical = canonicalizer.toCanonicalJson(semantic);
        return HashUtils.sha256("semantic:" + canonical);
    }

    public String deriveSimKey(IntentIrDocument source) {
        String content = buildSimContent(normalizeForKey(source));
        long fingerprint = simHash64(content);
        return Long.toUnsignedString(fingerprint, 16);
    }

    public int simDistance(String leftSimKeyHex, String rightSimKeyHex) {
        long left = Long.parseUnsignedLong(leftSimKeyHex, 16);
        long right = Long.parseUnsignedLong(rightSimKeyHex, 16);
        return Long.bitCount(left ^ right);
    }

    public boolean isNearDuplicate(String leftSimKeyHex, String rightSimKeyHex, int maxDistance) {
        return simDistance(leftSimKeyHex, rightSimKeyHex) <= maxDistance;
    }

    private IntentIrDocument toSemanticProjection(IntentIrDocument source) {
        Objects.requireNonNull(source, "source must not be null");
        IntentIrDocument normalized = source;

        Map<String, Object> filteredMeta = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : normalized.meta().entrySet()) {
            if (!VOLATILE_META_KEYS.contains(entry.getKey())) {
                filteredMeta.put(entry.getKey(), entry.getValue());
            }
        }
        return new IntentIrDocument(
            normalized.schemaVersion(),
            normalized.domain(),
            normalized.action(),
            normalized.input(),
            filteredMeta
        );
    }

    private String buildSimContent(IntentIrDocument source) {
        IntentIrDocument normalized = source;
        List<String> tokens = new ArrayList<>();
        tokens.add(normalized.domain().toLowerCase(Locale.ROOT));
        tokens.add(normalized.action().toLowerCase(Locale.ROOT));
        collectTokens(normalized.input(), tokens);
        collectTokens(normalized.meta(), tokens);
        tokens.sort(Comparator.naturalOrder());
        return String.join(" ", tokens);
    }

    @SuppressWarnings("unchecked")
    private void collectTokens(Object value, List<String> output) {
        if (value == null) {
            return;
        }
        if (value instanceof String text) {
            for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9_]+")) {
                if (!token.isBlank()) {
                    output.add(token);
                }
            }
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            output.add(String.valueOf(value));
            return;
        }
        if (value instanceof Map<?, ?> map) {
            map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(String::valueOf)))
                .forEach(entry -> {
                    output.add(String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT));
                    collectTokens(entry.getValue(), output);
                });
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collectTokens(item, output);
            }
            return;
        }
        output.add(String.valueOf(value).toLowerCase(Locale.ROOT));
    }

    private long simHash64(String text) {
        int[] bitCounts = new int[64];
        String[] terms = text.split("\\s+");
        for (String term : terms) {
            if (term.isBlank()) {
                continue;
            }
            long hash = fnv1a64(term);
            for (int bit = 0; bit < 64; bit++) {
                long mask = 1L << bit;
                if ((hash & mask) != 0L) {
                    bitCounts[bit] += 1;
                } else {
                    bitCounts[bit] -= 1;
                }
            }
        }
        long fingerprint = 0L;
        for (int bit = 0; bit < 64; bit++) {
            if (bitCounts[bit] >= 0) {
                fingerprint |= (1L << bit);
            }
        }
        return fingerprint;
    }

    private long fnv1a64(String text) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < text.length(); i++) {
            hash ^= text.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private IntentIrDocument normalizeForKey(IntentIrDocument source) {
        Objects.requireNonNull(source, "source must not be null");
        return new DefaultIntentIrNormalizer().normalize(source);
    }
}
