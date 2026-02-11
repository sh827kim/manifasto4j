package ai.manifesto.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KR: HostContext는 실행 시점의 컨텍스트 값(시간, 환경, 상태 참조 등)을 전달하는 타입입니다.
 * EN: HostContext is a context type carrying runtime values such as time, environment, and state references.
 */
public final class HostContext {
    private final long now;
    private final String randomSeed;
    private final Map<String, Object> env;
    private final Long durationMs;

    private HostContext(long now, String randomSeed, Map<String, Object> env, Long durationMs) {
        this.now = now;
        this.randomSeed = randomSeed != null ? randomSeed : "";
        this.env = env != null ? new HashMap<>(env) : null;
        this.durationMs = durationMs;
    }

    public long getNow() {
        return now;
    }

    public String getRandomSeed() {
        return randomSeed;
    }

    public Map<String, Object> getEnv() {
        return env != null ? new HashMap<>(env) : null;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public static HostContext of(long now, String randomSeed) {
        return new HostContext(now, randomSeed, null, null);
    }

    public static HostContext systemNow(String randomSeed) {
        return new HostContext(1L, randomSeed, null, null);
    }

    public static HostContext forSnapshot(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot is required");
        return new HostContext(
            snapshot.getMeta().getTimestamp(),
            snapshot.getMeta().getRandomSeed(),
            null,
            null
        );
    }

    public static Builder builder(long now, String randomSeed) {
        return new Builder(now, randomSeed);
    }

    public static final class Builder {
        private final long now;
        private final String randomSeed;
        private Map<String, Object> env;
        private Long durationMs;

        private Builder(long now, String randomSeed) {
            this.now = now;
            this.randomSeed = randomSeed;
        }

        public Builder env(Map<String, Object> env) {
            this.env = env != null ? new HashMap<>(env) : null;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public HostContext build() {
            return new HostContext(now, randomSeed, env, durationMs);
        }
    }
}
