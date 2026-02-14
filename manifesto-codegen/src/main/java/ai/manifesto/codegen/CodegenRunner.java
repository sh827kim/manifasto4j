package ai.manifesto.codegen;

import java.util.List;
import java.util.Objects;

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
        Objects.requireNonNull(request, "request must not be null");
        CodegenTarget target = Objects.requireNonNull(request.target(), "request.target must not be null");
        CodegenPlugin plugin = registry.resolve(target)
            .orElseThrow(() -> new IllegalArgumentException("No codegen plugin found for target: " + target.name()));
        return plugin.generate(request);
    }
}
