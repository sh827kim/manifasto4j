package ai.manifesto.host;

/**
 * HostRuntime 루프 경계 옵션.
 *
 * timeoutSeconds와 maxIterations를 분리해 운영 정책을 명시적으로 조정할 수 있다.
 */
public final class HostRuntimeOptions {
    private final int timeoutSeconds;
    private final int maxIterations;

    private HostRuntimeOptions(int timeoutSeconds, int maxIterations) {
        this.timeoutSeconds = timeoutSeconds;
        this.maxIterations = maxIterations;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public static HostRuntimeOptions forTimeoutSeconds(int timeoutSeconds) {
        int normalizedTimeout = Math.max(1, timeoutSeconds);
        return new HostRuntimeOptions(normalizedTimeout, normalizedTimeout * 100);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer timeoutSeconds;
        private Integer maxIterations;

        private Builder() {}

        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder maxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
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
            return new HostRuntimeOptions(normalizedTimeout, resolvedMaxIterations);
        }
    }
}
