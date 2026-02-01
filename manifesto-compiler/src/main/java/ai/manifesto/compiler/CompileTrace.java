package ai.manifesto.compiler;

import java.util.Map;

/**
 * CompileTrace - compilation phase timing info
 */
public record CompileTrace(
    String phase,
    long durationMs,
    Map<String, Object> details
) {
    public static CompileTrace of(String phase, long durationMs) {
        return new CompileTrace(phase, durationMs, null);
    }

    public static CompileTrace of(String phase, long durationMs, Map<String, Object> details) {
        return new CompileTrace(phase, durationMs, details);
    }
}
