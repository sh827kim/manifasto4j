package ai.manifesto.codegen.runtime;

import ai.manifesto.codegen.GeneratedArtifact;

import java.util.List;
import java.util.Map;

/**
 * KR: plugin 실행 결과(파일/아티팩트/진단)를 묶은 모델입니다.
 * EN: Plugin execution result model (files/artifacts/diagnostics).
 */
public record CodegenPluginResult(
    List<GeneratedArtifact> files,
    Map<String, Object> artifacts,
    List<CodegenDiagnostic> diagnostics
) {
    public CodegenPluginResult {
        files = files == null ? List.of() : List.copyOf(files);
        artifacts = artifacts == null ? Map.of() : Map.copyOf(artifacts);
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }

    public static CodegenPluginResult of(List<GeneratedArtifact> files) {
        return new CodegenPluginResult(files, Map.of(), List.of());
    }
}
