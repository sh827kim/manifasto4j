package ai.manifesto.host;

/**
 * KR: HostRuntimeOptions는 host 루프의 반복 한도, 타임아웃, effect 재시도 정책을 정의하는 설정 객체입니다.
 * EN: HostRuntimeOptions defines host-loop limits, timeout, and effect retry policies.
 */
public final class HostRuntimeOptions {
    private final int timeoutSeconds;
    private final int maxIterations;
    private final int maxEffectRetries;
    private final long maxEffectDurationMillis;

    private HostRuntimeOptions(
        int timeoutSeconds,
        int maxIterations,
        int maxEffectRetries,
        long maxEffectDurationMillis
    ) {
        this.timeoutSeconds = timeoutSeconds;
        this.maxIterations = maxIterations;
        this.maxEffectRetries = maxEffectRetries;
        this.maxEffectDurationMillis = maxEffectDurationMillis;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxEffectRetries() {
        return maxEffectRetries;
    }

    public long getMaxEffectDurationMillis() {
        return maxEffectDurationMillis;
    }

    public static HostRuntimeOptions forTimeoutSeconds(int timeoutSeconds) {
        int normalizedTimeout = Math.max(1, timeoutSeconds);
        return new HostRuntimeOptions(normalizedTimeout, normalizedTimeout * 100, 0, 0L);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * KR: HostRuntimeOptions 생성용 빌더입니다.
     * EN: Builder for creating HostRuntimeOptions.
     */
    public static final class Builder {
        private Integer timeoutSeconds;
        private Integer maxIterations;
        private Integer maxEffectRetries;
        private Long maxEffectDurationMillis;

        private Builder() {}

        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder maxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
            return this;
        }

        public Builder maxEffectRetries(int maxEffectRetries) {
            this.maxEffectRetries = maxEffectRetries;
            return this;
        }

        public Builder maxEffectDurationMillis(long maxEffectDurationMillis) {
            this.maxEffectDurationMillis = maxEffectDurationMillis;
            return this;
        }

        public HostRuntimeOptions build() {
            int resolvedTimeout = timeoutSeconds != null ? timeoutSeconds : 5;
            int normalizedTimeout = Math.max(1, resolvedTimeout);
            int resolvedMaxIterations = maxIterations != null
                ? maxIterations
                : normalizedTimeout * 100;
            if (resolvedMaxIterations < 1) {
                throw new IllegalArgumentException("maxIterations must be >= 1");
            }
            int resolvedMaxEffectRetries = maxEffectRetries != null ? maxEffectRetries : 0;
            if (resolvedMaxEffectRetries < 0) {
                throw new IllegalArgumentException("maxEffectRetries must be >= 0");
            }
            long resolvedMaxEffectDurationMillis = maxEffectDurationMillis != null ? maxEffectDurationMillis : 0L;
            if (resolvedMaxEffectDurationMillis < 0L) {
                throw new IllegalArgumentException("maxEffectDurationMillis must be >= 0");
            }
            return new HostRuntimeOptions(
                normalizedTimeout,
                resolvedMaxIterations,
                resolvedMaxEffectRetries,
                resolvedMaxEffectDurationMillis
            );
        }
    }
}
