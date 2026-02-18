package ai.manifesto.intentir;

import ai.manifesto.core.utils.HashUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * KR: Intent IR 문서에서 strict/semantic/sim 키를 파생하는 유틸리티입니다.
 * EN: Utility for deriving strict/semantic/sim keys from an Intent IR document.
 */
public final class IntentIrKeyDeriver {
    private final IntentIrCanonicalizer canonicalizer;

    public IntentIrKeyDeriver() {
        this(new IntentIrCanonicalizer());
    }

    public IntentIrKeyDeriver(IntentIrCanonicalizer canonicalizer) {
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer must not be null");
    }

    public String deriveStrictKey(IntentIrDocument source) {
        String canonical = canonicalizer.toStrictCanonicalJson(source);
        return HashUtils.sha256("strict:" + canonical);
    }

    public String deriveSemanticKey(IntentIrDocument source) {
        String canonical = canonicalizer.toSemanticCanonicalJson(source);
        return HashUtils.sha256("semantic:" + canonical);
    }

    public String deriveSimKey(IntentIrDocument source) {
        return Long.toUnsignedString(deriveSimFingerprint(source), 16);
    }

    public long deriveSimFingerprint(IntentIrDocument source) {
        String content = buildSimContent(canonicalizer.canonicalizeSemantic(source));
        return simHash64(content);
    }

    public int simDistance(String leftSimKeyHex, String rightSimKeyHex) {
        long left = parseSimKey(leftSimKeyHex);
        long right = parseSimKey(rightSimKeyHex);
        return simDistance(left, right);
    }

    public int simDistance(long leftFingerprint, long rightFingerprint) {
        return Long.bitCount(leftFingerprint ^ rightFingerprint);
    }

    public int simDistance(IntentIrDocument left, IntentIrDocument right) {
        return simDistance(deriveSimFingerprint(left), deriveSimFingerprint(right));
    }

    public boolean isNearDuplicate(long leftFingerprint, long rightFingerprint, int maxDistance) {
        return simDistance(leftFingerprint, rightFingerprint) <= maxDistance;
    }

    public boolean isNearDuplicate(IntentIrDocument left, IntentIrDocument right, int maxDistance) {
        return simDistance(left, right) <= maxDistance;
    }

    public long parseSimKey(String simKeyHex) {
        Objects.requireNonNull(simKeyHex, "simKeyHex must not be null");
        String normalized = simKeyHex.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("simKeyHex must not be blank");
        }
        return Long.parseUnsignedLong(normalized, 16);
    }

    public String formatSimKey(long fingerprint) {
        return Long.toUnsignedString(fingerprint, 16);
    }

    public int simDistanceHex(String leftSimKeyHex, String rightSimKeyHex) {
        return simDistance(leftSimKeyHex, rightSimKeyHex);
    }

    public int maxSimDistanceBits() {
        return 64;
    }

    public boolean isNearDuplicateHex(String leftSimKeyHex, String rightSimKeyHex, int maxDistance) {
        return simDistance(leftSimKeyHex, rightSimKeyHex) <= maxDistance;
    }

    public boolean isNearDuplicate(String leftSimKeyHex, String rightSimKeyHex, int maxDistance) {
        return simDistance(leftSimKeyHex, rightSimKeyHex) <= maxDistance;
    }

    private String buildSimContent(IntentIrDocument source) {
        List<String> tokens = new ArrayList<>();
        tokens.add(source.domain().toLowerCase(Locale.ROOT));
        tokens.add(source.action().toLowerCase(Locale.ROOT));
        collectTokens(source.input(), tokens);
        collectTokens(source.meta(), tokens);
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
}
