package ai.manifesto.core.utils;

import ai.manifesto.core.Snapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KR: SnapshotStoreUtils는 재사용 가능한 정적 보조 함수를 제공하는 유틸리티 타입입니다.
 * EN: SnapshotStoreUtils is a utility type providing reusable static helper functions.
 */
public final class SnapshotStoreUtils {
    private static final String PLATFORM_NAMESPACE_PREFIX = "$";

    private SnapshotStoreUtils() {
    }

    /**
     * 스냅샷을 storage canonical 형태로 정규화한다.
     * - data: $-prefix top-level key 제거 + 재귀 정렬/복사
     * - computed/input: 재귀 정렬/복사
     */
    public static Snapshot canonicalizeForStorage(Snapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        Map<String, Object> filteredData = stripPlatformNamespaces(snapshot.getData());
        Map<String, Object> canonicalData = deepSortMap(filteredData);
        Map<String, Object> canonicalComputed = deepSortMap(snapshot.getComputed());
        Map<String, Object> canonicalInput = deepSortMap(snapshot.getInput());

        return Snapshot.builder()
            .data(canonicalData)
            .computed(canonicalComputed)
            .system(snapshot.getSystem())
            .input(canonicalInput)
            .meta(snapshot.getMeta())
            .build();
    }

    /**
     * 플랫폼 네임스페이스 제거 없이 deep-copy + 정렬만 수행한다.
     */
    public static Snapshot deepCopySnapshot(Snapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        return Snapshot.builder()
            .data(deepSortMap(snapshot.getData()))
            .computed(deepSortMap(snapshot.getComputed()))
            .system(snapshot.getSystem())
            .input(deepSortMap(snapshot.getInput()))
            .meta(snapshot.getMeta())
            .build();
    }

    private static Map<String, Object> stripPlatformNamespaces(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!isPlatformNamespace(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static boolean isPlatformNamespace(String key) {
        return key != null && key.startsWith(PLATFORM_NAMESPACE_PREFIX);
    }

    private static Map<String, Object> deepSortMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return new LinkedHashMap<>();
        }

        List<String> keys = new ArrayList<>(map.keySet());
        keys.sort(String::compareTo);

        Map<String, Object> sorted = new LinkedHashMap<>();
        for (String key : keys) {
            sorted.put(key, deepCopyValue(map.get(key)));
        }
        return sorted;
    }

    @SuppressWarnings("unchecked")
    private static Object deepCopyValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> casted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                casted.put(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
            }
            return deepSortMap(casted);
        }
        if (value instanceof List<?> list) {
            List<Object> copied = new ArrayList<>(list.size());
            for (Object item : list) {
                copied.add(deepCopyValue(item));
            }
            return copied;
        }
        return value;
    }
}
