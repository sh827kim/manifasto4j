package ai.manifesto.compiler;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;
import java.util.Map;

/**
 * CompilePatchResult - runtime patch compile result
 */
public record CompilePatchResult(
    List<Map<String, Object>> ops,
    List<Diagnostic> diagnostics
) {
}
