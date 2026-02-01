package ai.manifesto.compiler;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.core.schema.DomainSchema;

import java.util.List;

/**
 * CompilationResult - 컴파일 결과
 */
public final class CompilationResult {
    private final DomainSchema schema;
    private final String error;
    private final List<Diagnostic> diagnostics;

    private CompilationResult(DomainSchema schema, String error, List<Diagnostic> diagnostics) {
        this.schema = schema;
        this.error = error;
        this.diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }

    public static CompilationResult ok(DomainSchema schema) {
        return new CompilationResult(schema, null, List.of());
    }

    public static CompilationResult ok(DomainSchema schema, List<Diagnostic> diagnostics) {
        return new CompilationResult(schema, null, diagnostics);
    }

    public static CompilationResult error(String message) {
        return new CompilationResult(null, message, List.of());
    }

    public static CompilationResult error(String message, List<Diagnostic> diagnostics) {
        return new CompilationResult(null, message, diagnostics);
    }

    public boolean isOk() {
        return schema != null;
    }

    public DomainSchema getSchema() {
        return schema;
    }

    public String getError() {
        return error;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
