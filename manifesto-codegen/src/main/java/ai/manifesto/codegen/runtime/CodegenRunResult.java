package ai.manifesto.codegen.runtime;

import ai.manifesto.codegen.GeneratedArtifact;

import java.util.List;

/**
 * KR: codegen runner 상세 실행 결과입니다.
 * EN: Detailed execution result from codegen runner.
 */
public record CodegenRunResult(
    List<GeneratedArtifact> files,
    List<CodegenDiagnostic> diagnostics,
    String schemaHash,
    CodegenPluginOptions pluginOptions
) {
    public boolean hasErrors() {
        return diagnostics != null && diagnostics.stream().anyMatch(d -> d.level() == CodegenDiagnosticLevel.ERROR);
    }
}
