package ai.manifesto.core.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: PathUtils는 재사용 가능한 정적 보조 함수를 제공하는 유틸리티 타입입니다.
 * EN: PathUtils is a utility type providing reusable static helper functions.
 */
public class PathUtils {

    /**
     * 경로로부터 값을 조회
     * 경로가 존재하지 않으면 null 반환
     *
     * @param obj 루트 객체
     * @param path 경로 (점 구분, 예: "user.name")
     * @return 경로의 값, 또는 null
     */
    public static Object getByPath(Object obj, String path) {
        if (obj == null || path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = obj;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            current = getProperty(current, part);
        }

        return current;
    }

    /**
     * 단일 레벨의 속성 조회
     */
    @SuppressWarnings("unchecked")
    private static Object getProperty(Object obj, String key) {
        if (obj == null) {
            return null;
        }

        // 맵인 경우
        if (obj instanceof Map) {
            return ((Map<String, Object>) obj).get(key);
        }

        // 리스트인 경우 (인덱스로 접근)
        if (obj instanceof List<?> list) {
            try {
                int index = Integer.parseInt(key);
                if (index >= 0 && index < list.size()) {
                    return list.get(index);
                }
                return null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // 다른 타입은 null 반환
        return null;
    }

    /**
     * 경로에 값을 설정
     * 중간 경로가 없으면 맵으로 생성
     * 원본 객체는 수정되지 않고, 새로운 복사본을 반환
     *
     * @param obj 루트 객체
     * @param path 경로
     * @param value 설정할 값
     * @return 수정된 새로운 객체
     */
    @SuppressWarnings("unchecked")
    public static Object setByPath(Object obj, String path, Object value) {
        if (path == null || path.isEmpty()) {
            return value;
        }

        String[] parts = path.split("\\.");

        if (parts.length == 1) {
            // 단일 레벨
            if (obj instanceof Map) {
                Map<String, Object> map = new java.util.HashMap<>((Map<String, Object>) obj);
                map.put(parts[0], value);
                return map;
            } else if (obj instanceof List) {
                try {
                    int index = Integer.parseInt(parts[0]);
                    List<Object> list = new java.util.ArrayList<>((List<?>) obj);
                    if (index >= 0 && index < list.size()) {
                        list.set(index, value);
                    } else if (index == list.size()) {
                        list.add(value);
                    }
                    return list;
                } catch (NumberFormatException e) {
                    return obj;
                }
            } else {
                // 맵으로 새로 생성
                Map<String, Object> map = new java.util.HashMap<>();
                map.put(parts[0], value);
                return map;
            }
        }

        // 재귀: 첫 레벨만 처리 후 나머지는 재귀
        String firstKey = parts[0];
        String restPath = String.join(".", java.util.Arrays.copyOfRange(parts, 1, parts.length));

        Object current = null;
        if (obj instanceof Map) {
            current = ((Map<String, Object>) obj).get(firstKey);
        } else if (obj instanceof List<?> list) {
            try {
                int index = Integer.parseInt(firstKey);
                if (index >= 0 && index < list.size()) {
                    current = list.get(index);
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        Object updated = setByPath(current, restPath, value);

        // 결과를 다시 obj에 집어넣기
        if (obj instanceof Map) {
            Map<String, Object> map = new java.util.HashMap<>((Map<String, Object>) obj);
            map.put(firstKey, updated);
            return map;
        } else if (obj instanceof List) {
            try {
                int index = Integer.parseInt(firstKey);
                List<Object> list = new java.util.ArrayList<>((List<?>) obj);
                if (index >= 0 && index < list.size()) {
                    list.set(index, updated);
                } else if (index == list.size()) {
                    list.add(updated);
                }
                return list;
            } catch (NumberFormatException e) {
                return obj;
            }
        } else {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put(firstKey, updated);
            return map;
        }
    }

    /**
     * 경로에 값을 제거 (unset)
     */
    @SuppressWarnings("unchecked")
    public static Object unsetByPath(Object obj, String path) {
        if (obj == null || path == null || path.isEmpty()) {
            return obj;
        }

        String[] parts = path.split("\\.");

        if (parts.length == 1) {
            if (obj instanceof Map) {
                Map<String, Object> map = new java.util.HashMap<>((Map<String, Object>) obj);
                map.remove(parts[0]);
                return map;
            }
            return obj;
        }

        // 재귀
        String firstKey = parts[0];
        String restPath = String.join(".", java.util.Arrays.copyOfRange(parts, 1, parts.length));

        Object current = null;
        if (obj instanceof Map) {
            current = ((Map<String, Object>) obj).get(firstKey);
        }

        Object updated = unsetByPath(current, restPath);

        if (obj instanceof Map) {
            Map<String, Object> map = new java.util.HashMap<>((Map<String, Object>) obj);
            map.put(firstKey, updated);
            return map;
        }

        return obj;
    }

    /**
     * 경로에 값을 병합 (merge - 얕은 병합)
     */
    @SuppressWarnings("unchecked")
    public static Object mergeByPath(Object obj, String path, Object mergeValue) {
        Object current = getByPath(obj, path);

        if (!(mergeValue instanceof Map)) {
            // merge 값이 맵이 아니면 set으로 동작
            return setByPath(obj, path, mergeValue);
        }

        Map<String, Object> merged = new java.util.HashMap<>();
        if (current instanceof Map) {
            merged.putAll((Map<String, Object>) current);
        }
        merged.putAll((Map<String, Object>) mergeValue);

        return setByPath(obj, path, merged);
    }

    /**
     * 경로 존재 여부 확인
     */
    public static boolean hasPath(Object obj, String path) {
        if (path == null || path.isEmpty()) {
            return obj != null;
        }

        String[] parts = path.split("\\.");
        Object current = obj;
        for (String part : parts) {
            if (current == null) {
                return false;
            }
            if (current instanceof Map<?, ?> map) {
                if (!map.containsKey(part)) {
                    return false;
                }
                current = map.get(part);
                continue;
            }
            if (current instanceof List<?> list) {
                try {
                    int index = Integer.parseInt(part);
                    if (index < 0 || index >= list.size()) {
                        return false;
                    }
                    current = list.get(index);
                    continue;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 부모 경로 반환
     */
    public static String parentPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int idx = path.lastIndexOf('.');
        return idx >= 0 ? path.substring(0, idx) : "";
    }

    /**
     * 마지막 세그먼트 반환
     */
    public static String lastSegment(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int idx = path.lastIndexOf('.');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }
}
