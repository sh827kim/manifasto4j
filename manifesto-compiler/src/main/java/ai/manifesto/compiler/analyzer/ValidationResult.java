package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;

/**
 * ValidationResult - semantic validation result
 */
public record ValidationResult(
    boolean valid,
    List<Diagnostic> diagnostics
) {
}
