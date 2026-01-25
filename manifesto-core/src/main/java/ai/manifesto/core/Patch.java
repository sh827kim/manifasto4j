package ai.manifesto.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Patch - 상태 변경 연산
 * 오직 3가지 연산만 존재한다: set, unset, merge
 *
 * Manifesto 핵심 원칙: "Three operations are enough"
 * 모든 상태 변경은 이 세 연산의 조합으로 표현된다.
 */
public sealed class Patch {

    protected final String path;    // 경로 (예: "data.todos.0.completed")

    protected Patch(String path) {
        this.path = path;
    }

    public String getPath() { return path; }

    /**
     * SetPatch - 경로의 값을 교체한다 (없으면 생성)
     * data.count = 5
     * data.todos = [...]
     */
    public static final class Set extends Patch {
        private final Object value;

        public Set(String path, Object value) {
            super(path);
            this.value = value;
        }

        public Object getValue() { return value; }

        @Override
        public String toString() {
            return "SetPatch{" +
                   "path='" + path + '\'' +
                   ", value=" + value +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Set set)) return false;
            return Objects.equals(path, set.path) &&
                   Objects.equals(value, set.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash("set", path, value);
        }
    }

    /**
     * UnsetPatch - 경로의 속성을 제거한다
     * unset data.user.email
     */
    public static final class Unset extends Patch {

        public Unset(String path) {
            super(path);
        }

        @Override
        public String toString() {
            return "UnsetPatch{" +
                   "path='" + path + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Unset unset)) return false;
            return Objects.equals(path, unset.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash("unset", path);
        }
    }

    /**
     * MergePatch - 경로에서 얕은 병합을 수행한다
     * merge data.user { "name": "Alice", "updated": true }
     * 기존 { "name": "Bob", "email": "bob@example.com" } →
     * 결과 { "name": "Alice", "email": "bob@example.com", "updated": true }
     */
    public static final class Merge extends Patch {
        private final Map<String, Object> value;

        public Merge(String path, Map<String, Object> value) {
            super(path);
            this.value = value != null ? new HashMap<>(value) : new HashMap<>();
        }

        public Map<String, Object> getValue() {
            return new HashMap<>(value);
        }

        @Override
        public String toString() {
            return "MergePatch{" +
                   "path='" + path + '\'' +
                   ", value=" + value +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Merge merge)) return false;
            return Objects.equals(path, merge.path) &&
                   Objects.equals(value, merge.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash("merge", path, value);
        }
    }

    /**
     * Patch 생성 헬퍼
     */
    public static Set set(String path, Object value) {
        return new Set(path, value);
    }

    public static Unset unset(String path) {
        return new Unset(path);
    }

    public static Merge merge(String path, Map<String, Object> value) {
        return new Merge(path, value);
    }
}
