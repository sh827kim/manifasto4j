package ai.manifesto.runtime;

/**
 * KR: memory 유지보수(정리) 옵션입니다.
 * EN: Maintenance options for memory pruning.
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
