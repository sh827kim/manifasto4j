package ai.manifesto.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * KR: Codegen plugin 등록/조회 레지스트리입니다.
 * EN: Registry for registering and resolving codegen plugins.
 */
public final class CodegenPluginRegistry {
    private final List<CodegenPlugin> plugins = new ArrayList<>();

    public CodegenPluginRegistry register(CodegenPlugin plugin) {
        plugins.add(Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    public Optional<CodegenPlugin> resolve(CodegenTarget target) {
        if (target == null) {
            return Optional.empty();
        }
        return plugins.stream().filter(plugin -> plugin.supports(target)).findFirst();
    }

    public List<CodegenPlugin> all() {
        return List.copyOf(plugins);
    }
}
