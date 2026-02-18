package ai.manifesto.codegen.runtime;

import java.util.Map;

/**
 * KR: plugin 실행 시 전달되는 누적 컨텍스트입니다.
 * EN: Accumulated context passed to each plugin execution.
 */
public record CodegenPluginContext(
    String sourceId,
    String schemaHash,
    String outDir,
    Map<String, Object> artifacts
) {
    public CodegenPluginContext {
        artifacts = artifacts == null ? Map.of() : Map.copyOf(artifacts);
    }
}
