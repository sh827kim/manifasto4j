package ai.manifesto.codegen.runtime;

import ai.manifesto.codegen.GeneratedArtifact;

import java.util.List;
import java.util.Map;

/**
 * KR: codegen runner 상세 실행 결과입니다.
 * EN: Detailed execution result from codegen runner.
 */
public record CodegenRunResult(
    List<GeneratedArtifact> files,
    List<CodegenDiagnostic> diagnostics,
    String schemaHash,
    CodegenPluginOptions pluginOptions,
    Map<String, Object> pluginArtifacts
) {
    public CodegenRunResult {
        files = files == null ? List.of() : List.copyOf(files);
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
        pluginArtifacts = pluginArtifacts == null ? Map.of() : Map.copyOf(pluginArtifacts);
    }

    public CodegenRunResult(
        List<GeneratedArtifact> files,
        List<CodegenDiagnostic> diagnostics,
        String schemaHash,
        CodegenPluginOptions pluginOptions
    ) {
        this(files, diagnostics, schemaHash, pluginOptions, Map.of());
    }

    public boolean hasErrors() {
        return diagnostics != null && diagnostics.stream().anyMatch(d -> d.level() == CodegenDiagnosticLevel.ERROR);
    }
}
