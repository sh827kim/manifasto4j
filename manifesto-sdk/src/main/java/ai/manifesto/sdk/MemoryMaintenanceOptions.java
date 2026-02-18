package ai.manifesto.sdk;

/**
 * KR: SDK memory 유지보수 옵션입니다.
 * EN: SDK memory maintenance options.
 */
public record MemoryMaintenanceOptions(int maxEntries) {
    public MemoryMaintenanceOptions {
        if (maxEntries < 0) {
            throw new IllegalArgumentException("maxEntries must be >= 0");
        }
    }

    public static MemoryMaintenanceOptions defaults() {
        return new MemoryMaintenanceOptions(Integer.MAX_VALUE);
    }
}
