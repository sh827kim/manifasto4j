package ai.manifesto.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: Snapshot는 Core 모듈에서 snapshot 역할을 수행하는 구현 타입입니다.
 * EN: Snapshot is an implementation type performing snapshot roles in the Core module.
 */
public class Snapshot {

    private final Map<String, Object> data;        // 도메인 데이터
    private final Map<String, Object> computed;    // 계산된 값
    private final SystemState system;              // 시스템 상태
    private final Map<String, Object> input;       // 현재 액션 입력
    private final SnapshotMeta meta;               // 메타데이터

    /**
     * 생성자 (불변)
     */
    private Snapshot(Map<String, Object> data, Map<String, Object> computed,
                     SystemState system, Map<String, Object> input,
                     SnapshotMeta meta) {
        this.data = new HashMap<>(data != null ? data : new HashMap<>());
        this.computed = new HashMap<>(computed != null ? computed : new HashMap<>());
        this.system = system != null ? system : SystemState.initial();
        this.input = new HashMap<>(input != null ? input : new HashMap<>());
        this.meta = meta != null ? meta : SnapshotMeta.create(0);
    }

    // ===== Getters =====
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    public Map<String, Object> getComputed() {
        return new HashMap<>(computed);
    }

    public SystemState getSystem() {
        return system;
    }

    public Map<String, Object> getInput() {
        return new HashMap<>(input);
    }

    public SnapshotMeta getMeta() {
        return meta;
    }

    // ===== Copy-on-Write 패턴 =====
    // 불변성을 유지하면서 특정 필드만 변경한 새로운 Snapshot 생성

    /**
     * data를 변경한 새로운 Snapshot 생성
     */
    public Snapshot withData(Map<String, Object> newData) {
        if (Objects.equals(newData, this.data)) return this;
        return new Snapshot(newData, computed, system, input, meta);
    }

    /**
     * computed를 변경한 새로운 Snapshot 생성
     */
    public Snapshot withComputed(Map<String, Object> newComputed) {
        if (Objects.equals(newComputed, this.computed)) return this;
        return new Snapshot(data, newComputed, system, input, meta);
    }

    /**
     * system을 변경한 새로운 Snapshot 생성
     */
    public Snapshot withSystem(SystemState newSystem) {
        if (Objects.equals(newSystem, this.system)) return this;
        return new Snapshot(data, computed, newSystem, input, meta);
    }

    /**
     * input을 변경한 새로운 Snapshot 생성
     */
    public Snapshot withInput(Map<String, Object> newInput) {
        if (Objects.equals(newInput, this.input)) return this;
        return new Snapshot(data, computed, system, newInput, meta);
    }

    /**
     * meta를 변경한 새로운 Snapshot 생성
     */
    public Snapshot withMeta(SnapshotMeta newMeta) {
        if (Objects.equals(newMeta, this.meta)) return this;
        return new Snapshot(data, computed, system, input, newMeta);
    }

    /**
     * 모든 필드를 변경한 새로운 Snapshot 생성
     */
    public Snapshot copy(Map<String, Object> data, Map<String, Object> computed,
                        SystemState system, Map<String, Object> input,
                        SnapshotMeta meta) {
        return new Snapshot(data, computed, system, input, meta);
    }

    /**
     * TS createSnapshot 대응 헬퍼
     */
    @SuppressWarnings("unchecked")
    public static Snapshot createSnapshot(Object data, String schemaHash, HostContext context) {
        Map<String, Object> dataMap = data instanceof Map<?, ?> map
            ? new HashMap<>((Map<String, Object>) map)
            : new HashMap<>();
        SnapshotMeta meta = SnapshotMeta.create(
            0,
            context.getNow(),
            context.getRandomSeed(),
            schemaHash
        );
        return new Snapshot(dataMap, new HashMap<>(), SystemState.initial(), new HashMap<>(), meta);
    }

    // ===== 빌더 =====
    public static class Builder {
        private Map<String, Object> data = new HashMap<>();
        private Map<String, Object> computed = new HashMap<>();
        private SystemState system = SystemState.initial();
        private Map<String, Object> input = new HashMap<>();
        private SnapshotMeta meta = SnapshotMeta.create(0);

        public Builder data(Map<String, Object> data) {
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
            return this;
        }

        public Builder computed(Map<String, Object> computed) {
            this.computed = computed != null ? new HashMap<>(computed) : new HashMap<>();
            return this;
        }

        public Builder system(SystemState system) {
            this.system = system != null ? system : SystemState.initial();
            return this;
        }

        public Builder input(Map<String, Object> input) {
            this.input = input != null ? new HashMap<>(input) : new HashMap<>();
            return this;
        }

        public Builder meta(SnapshotMeta meta) {
            this.meta = meta;
            return this;
        }

        public Snapshot build() {
            return new Snapshot(data, computed, system, input, meta);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 초기 Snapshot 생성
     */
    public static Snapshot initial() {
        return new Snapshot(new HashMap<>(), new HashMap<>(),
            SystemState.initial(), new HashMap<>(),
            SnapshotMeta.create(0));
    }

    @Override
    public String toString() {
        return "Snapshot{" +
               "version=" + meta.getVersion() +
               ", status=" + system.getStatus() +
               ", dataKeys=" + data.keySet() +
               ", computedKeys=" + computed.keySet() +
               ", timestamp=" + meta.getTimestamp() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Snapshot snapshot)) return false;
        return Objects.equals(data, snapshot.data) &&
               Objects.equals(computed, snapshot.computed) &&
               Objects.equals(system, snapshot.system) &&
               Objects.equals(input, snapshot.input) &&
               Objects.equals(meta, snapshot.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, computed, system, input, meta);
    }

    // ===== 메타데이터 =====
    /**
     * SnapshotMeta - Snapshot의 메타정보
     */
    public static class SnapshotMeta {
        private final long version;           // 단조증가 버전
        private final long timestamp;         // 생성 시각 (ms)
        private final String randomSeed;      // 결정론적 난수 생성용
        private final String schemaHash;      // 스키마 해시

        private SnapshotMeta(long version, long timestamp, String randomSeed,
                            String schemaHash) {
            this.version = version;
            this.timestamp = timestamp;
            this.randomSeed = randomSeed != null ? randomSeed : "";
            this.schemaHash = schemaHash != null ? schemaHash : "";
        }

        public static SnapshotMeta create(long version) {
            long deterministicTimestamp = version + 1;
            return new SnapshotMeta(version, deterministicTimestamp, "", "");
        }

        public static SnapshotMeta create(long version, long timestamp,
                                         String randomSeed, String schemaHash) {
            return new SnapshotMeta(version, timestamp, randomSeed, schemaHash);
        }

        // Getters
        public long getVersion() { return version; }
        public long getTimestamp() { return timestamp; }
        public String getRandomSeed() { return randomSeed; }
        public String getSchemaHash() { return schemaHash; }

        /**
         * 버전을 증가시킨 새로운 메타데이터 생성
         */
        public SnapshotMeta nextVersion() {
            return new SnapshotMeta(version + 1, timestamp + 1, randomSeed, schemaHash);
        }

        /**
         * 버전/타임스탬프를 지정해 새로운 메타데이터 생성
         */
        public SnapshotMeta nextVersion(long timestamp) {
            return new SnapshotMeta(version + 1, timestamp, randomSeed, schemaHash);
        }

        /**
         * 타임스탬프를 업데이트한 새로운 메타데이터 생성
         */
        public SnapshotMeta withTimestamp(long timestamp) {
            return new SnapshotMeta(version, timestamp, randomSeed, schemaHash);
        }

        @Override
        public String toString() {
            return "SnapshotMeta{" +
                   "version=" + version +
                   ", timestamp=" + timestamp +
                   ", schemaHash='" + schemaHash + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SnapshotMeta that)) return false;
            return version == that.version &&
                   timestamp == that.timestamp &&
                   Objects.equals(randomSeed, that.randomSeed) &&
                   Objects.equals(schemaHash, that.schemaHash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, timestamp, randomSeed, schemaHash);
        }
    }
}
