package ai.manifesto.compiler;

import java.util.Map;

/**
 * KR: CompileTrace는 컴파일러 모듈에서 전달되는 compile trace 데이터를 담는 불변 레코드입니다.
 * EN: CompileTrace is an immutable record carrying compile trace data in the compiler module.
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
