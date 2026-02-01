package ai.manifesto.compiler;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.core.schema.DomainSchema;

import java.util.List;

/**
 * GenerateResult - AST to DomainSchema 결과
 */
public record GenerateResult(
    DomainSchema schema,
    List<Diagnostic> diagnostics
) {
}
