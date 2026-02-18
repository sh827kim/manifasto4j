package ai.manifesto.codegen;

import ai.manifesto.codegen.runtime.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
        Map<String, Object> allArtifacts = new LinkedHashMap<>();
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
        applyPluginOutput(
            request,
            safeOptions,
            effectivePluginOptions,
            diagnostics,
            header,
            vfs,
            plugin,
            schemaHash,
            allArtifacts
        );

        List<GeneratedArtifact> files = vfs.getFiles().stream()
            .map(file -> new GeneratedArtifact(file.path(), file.content()))
            .toList();
        diagnostics.addAll(flushFilesIfRequested(files, diagnostics, safeOptions));

        return new CodegenRunResult(
            files,
            List.copyOf(diagnostics),
            schemaHash,
            effectivePluginOptions,
            Map.copyOf(allArtifacts)
        );
    }

    public CodegenRunResult generateComposite(
        CodegenRequest request,
        CodegenExecutionOptions options,
        List<CodegenTarget> orderedTargets
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(orderedTargets, "orderedTargets must not be null");
        CodegenExecutionOptions safeOptions = options == null ? CodegenExecutionOptions.defaults() : options;
        CodegenPluginOptions effectivePluginOptions = safeOptions.pluginOptions() == null
            ? CodegenPluginOptions.defaults()
            : safeOptions.pluginOptions();

        List<CodegenDiagnostic> diagnostics = new ArrayList<>();
        Map<String, Object> allArtifacts = new LinkedHashMap<>();
        diagnostics.addAll(validatePluginIds());
        diagnostics.addAll(effectivePluginOptions.validate());

        String schemaHash = StableHash.stableHash(request.schema());
        String header = HeaderGenerator.generateHeader(new HeaderOptions(
            safeOptions.sourceId(),
            schemaHash,
            safeOptions.stamp()
        ));
        VirtualFileSystem vfs = new VirtualFileSystem();

        for (CodegenTarget target : orderedTargets) {
            if (target == null) {
                diagnostics.add(CodegenDiagnostic.error("runner", "Composite target must not be null"));
                continue;
            }
            CodegenPlugin plugin = registry.resolve(target).orElse(null);
            if (plugin == null) {
                diagnostics.add(CodegenDiagnostic.error("runner", "No codegen plugin found for target: " + target.name()));
                continue;
            }
            CodegenRequest perTargetRequest = new CodegenRequest(
                request.schema(),
                request.basePackage(),
                target
            );
            applyPluginOutput(
                perTargetRequest,
                safeOptions,
                effectivePluginOptions,
                diagnostics,
                header,
                vfs,
                plugin,
                schemaHash,
                allArtifacts
            );
        }

        List<GeneratedArtifact> files = vfs.getFiles().stream()
            .map(file -> new GeneratedArtifact(file.path(), file.content()))
            .toList();
        diagnostics.addAll(flushFilesIfRequested(files, diagnostics, safeOptions));

        return new CodegenRunResult(
            files,
            List.copyOf(diagnostics),
            schemaHash,
            effectivePluginOptions,
            Map.copyOf(allArtifacts)
        );
    }

    private void applyPluginOutput(
        CodegenRequest request,
        CodegenExecutionOptions safeOptions,
        CodegenPluginOptions effectivePluginOptions,
        List<CodegenDiagnostic> diagnostics,
        String header,
        VirtualFileSystem vfs,
        CodegenPlugin plugin,
        String schemaHash,
        Map<String, Object> allArtifacts
    ) {
        CodegenPluginResult pluginResult;
        try {
            CodegenPluginContext pluginContext = new CodegenPluginContext(
                safeOptions.sourceId(),
                schemaHash,
                safeOptions.outDir(),
                Map.copyOf(allArtifacts)
            );
            pluginResult = plugin.generateWithContext(request, effectivePluginOptions, pluginContext);
        } catch (RuntimeException error) {
            diagnostics.add(CodegenDiagnostic.error(plugin.pluginId(), "Plugin threw: " + error.getMessage()));
            return;
        }
        diagnostics.addAll(pluginResult.diagnostics());

        for (GeneratedArtifact artifact : pluginResult.files()) {
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

        if (!pluginResult.artifacts().isEmpty()) {
            allArtifacts.put(plugin.pluginId(), Map.copyOf(pluginResult.artifacts()));
        }
    }

    private List<CodegenDiagnostic> flushFilesIfRequested(
        List<GeneratedArtifact> files,
        List<CodegenDiagnostic> currentDiagnostics,
        CodegenExecutionOptions safeOptions
    ) {
        boolean hasErrors = currentDiagnostics.stream().anyMatch(d -> d.level() == CodegenDiagnosticLevel.ERROR);
        if (hasErrors) {
            return List.of();
        }
        if (!safeOptions.flushToDisk()) {
            return List.of();
        }
        if (safeOptions.outDir() == null || safeOptions.outDir().isBlank()) {
            return List.of(CodegenDiagnostic.error(
                "runner",
                "outDir is required when flushToDisk is enabled"
            ));
        }
        try {
            Path outDirPath = Paths.get(safeOptions.outDir());
            writeOutputs(files, outDirPath, safeOptions.cleanOutDir());
            return List.of();
        } catch (InvalidPathException exception) {
            return List.of(CodegenDiagnostic.error(
                "runner",
                "Invalid outDir path: " + exception.getMessage()
            ));
        } catch (IOException exception) {
            return List.of(CodegenDiagnostic.error(
                "runner",
                "Failed to flush generated files: " + exception.getMessage()
            ));
        }
    }

    private void writeOutputs(
        List<GeneratedArtifact> files,
        Path outDirPath,
        boolean cleanOutDir
    ) throws IOException {
        if (cleanOutDir && Files.exists(outDirPath)) {
            deleteDirectoryRecursively(outDirPath);
        }
        Files.createDirectories(outDirPath);

        for (GeneratedArtifact file : files) {
            Path filePath = outDirPath.resolve(file.relativePath().replace("/", java.io.File.separator));
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(filePath, file.content(), StandardCharsets.UTF_8);
        }
    }

    private void deleteDirectoryRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var pathStream = Files.walk(root)) {
            List<Path> paths = pathStream
                .sorted((left, right) -> right.compareTo(left))
                .toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
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
