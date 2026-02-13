package ai.manifesto.intentir;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * KR: Intent IR 최소 정규화 구현으로 필수 필드 검증 및 map key 정렬 복사를 제공합니다.
 * EN: Minimal Intent IR normalizer that validates required fields and returns key-sorted map copies.
 */
public final class DefaultIntentIrNormalizer implements IntentIrNormalizer {

    @Override
    public IntentIrDocument normalize(IntentIrDocument source) {
        Objects.requireNonNull(source, "source must not be null");
        requireText(source.schemaVersion(), "schemaVersion");
        requireText(source.domain(), "domain");
        requireText(source.action(), "action");

        Map<String, Object> normalizedInput = sortedCopy(source.input());
        Map<String, Object> normalizedMeta = sortedCopy(source.meta());

        return new IntentIrDocument(
            source.schemaVersion().trim(),
            source.domain().trim(),
            source.action().trim(),
            normalizedInput,
            normalizedMeta
        );
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private static Map<String, Object> sortedCopy(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Object> sorted = new TreeMap<>(source);
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            normalized.put(entry.getKey(), deepNormalize(entry.getValue()));
        }
        return Collections.unmodifiableMap(normalized);
    }

    @SuppressWarnings("unchecked")
    private static Object deepNormalize(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            TreeMap<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                sorted.put(String.valueOf(entry.getKey()), deepNormalize(entry.getValue()));
            }
            return Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
        }
        if (value instanceof List<?> listValue) {
            List<Object> normalized = new ArrayList<>(listValue.size());
            for (Object item : listValue) {
                normalized.add(deepNormalize(item));
            }
            return Collections.unmodifiableList(normalized);
        }
        return value;
    }
}
