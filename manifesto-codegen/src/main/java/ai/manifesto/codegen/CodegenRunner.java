package ai.manifesto.codegen;

import ai.manifesto.codegen.runtime.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * KR: 요청 target에 맞는 plugin을 선택해 코드를 생성하는 실행기입니다.
 * EN: Runner that selects a matching plugin for the request target and generates artifacts.
 */
public final class CodegenRunner implements CodeGenerator {
    private final CodegenPluginRegistry registry;

    public CodegenRunner(CodegenPluginRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    public static CodegenRunner withDefaults() {
        CodegenPluginRegistry registry = new CodegenPluginRegistry()
            .register(new JavaDtoCodeGenerator())
            .register(new JavaTypedClientCodeGenerator());
        return new CodegenRunner(registry);
    }

    @Override
    public List<GeneratedArtifact> generate(CodegenRequest request) {
        CodegenRunResult result = generateDetailed(request, CodegenExecutionOptions.defaults());
        if (result.hasErrors()) {
            String message = result.diagnostics().stream()
                .filter(diagnostic -> diagnostic.level() == CodegenDiagnosticLevel.ERROR)
                .map(CodegenDiagnostic::message)
                .findFirst()
                .orElse("Codegen failed with unknown error");
            throw new IllegalStateException(message);
        }
        return result.files();
    }

    public CodegenRunResult generateDetailed(CodegenRequest request, CodegenExecutionOptions options) {
        Objects.requireNonNull(request, "request must not be null");
        CodegenTarget target = Objects.requireNonNull(request.target(), "request.target must not be null");
        CodegenExecutionOptions safeOptions = options == null ? CodegenExecutionOptions.defaults() : options;
        CodegenPluginOptions effectivePluginOptions = safeOptions.pluginOptions() == null
            ? CodegenPluginOptions.defaults()
            : safeOptions.pluginOptions();

        List<CodegenDiagnostic> diagnostics = new ArrayList<>();
        diagnostics.addAll(validatePluginIds());
        diagnostics.addAll(effectivePluginOptions.validate());

        CodegenPlugin plugin = registry.resolve(target)
            .orElse(null);
        if (plugin == null) {
            throw new IllegalArgumentException("No codegen plugin found for target: " + target.name());
        }

        String schemaHash = StableHash.stableHash(request.schema());
        String header = HeaderGenerator.generateHeader(new HeaderOptions(
            safeOptions.sourceId(),
            schemaHash,
            safeOptions.stamp()
        ));

        VirtualFileSystem vfs = new VirtualFileSystem();
        List<GeneratedArtifact> generated;
        try {
            generated = plugin.generate(request, effectivePluginOptions);
        } catch (RuntimeException error) {
            diagnostics.add(CodegenDiagnostic.error(plugin.pluginId(), "Plugin threw: " + error.getMessage()));
            return new CodegenRunResult(List.of(), List.copyOf(diagnostics), schemaHash, effectivePluginOptions);
        }

        for (GeneratedArtifact artifact : generated) {
            String relativePath = artifact.relativePath() == null ? "" : artifact.relativePath();
            PathValidationResult validation = PathSafety.validatePath(relativePath);
            if (!validation.valid()) {
                diagnostics.add(CodegenDiagnostic.error(
                    plugin.pluginId(),
                    "Invalid path \"" + relativePath + "\": " + validation.reason()
                ));
                continue;
            }

            String content = artifact.content() == null ? "" : artifact.content();
            if (safeOptions.prependGeneratedHeader()) {
                content = header + content;
            }
            CodegenDiagnostic collision = vfs.applyPatch(FilePatch.set(validation.normalized(), content), plugin.pluginId());
            if (collision != null) {
                diagnostics.add(collision);
            }
        }

        List<GeneratedArtifact> files = vfs.getFiles().stream()
            .map(file -> new GeneratedArtifact(file.path(), file.content()))
            .toList();

        return new CodegenRunResult(
            files,
            List.copyOf(diagnostics),
            schemaHash,
            effectivePluginOptions
        );
    }

    private List<CodegenDiagnostic> validatePluginIds() {
        Set<String> seen = new LinkedHashSet<>();
        List<CodegenDiagnostic> diagnostics = new ArrayList<>();
        for (CodegenPlugin plugin : registry.all()) {
            String id = plugin.pluginId();
            if (id == null || id.isBlank()) {
                diagnostics.add(CodegenDiagnostic.error("registry", "Plugin id must not be blank"));
                continue;
            }
            if (!seen.add(id)) {
                diagnostics.add(CodegenDiagnostic.error("registry", "Duplicate plugin id: " + id));
            }
        }
        return List.copyOf(diagnostics);
    }
}
