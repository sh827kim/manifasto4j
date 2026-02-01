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
    private final List<Diagnostic> warnings;
    private final List<Diagnostic> errors;
    private final List<CompileTrace> trace;

    private CompilationResult(DomainSchema schema, String error, List<Diagnostic> diagnostics, List<CompileTrace> trace) {
        this.schema = schema;
        this.error = error;
        this.diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
        this.warnings = this.diagnostics.stream()
            .filter(d -> d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.WARNING)
            .toList();
        this.errors = this.diagnostics.stream()
            .filter(d -> d.severity() == ai.manifesto.compiler.diagnostics.DiagnosticSeverity.ERROR)
            .toList();
        this.trace = trace == null ? List.of() : List.copyOf(trace);
    }

    public static CompilationResult ok(DomainSchema schema) {
        return new CompilationResult(schema, null, List.of(), List.of());
    }

    public static CompilationResult ok(DomainSchema schema, List<Diagnostic> diagnostics) {
        return new CompilationResult(schema, null, diagnostics, List.of());
    }

    public static CompilationResult ok(DomainSchema schema, List<Diagnostic> diagnostics, List<CompileTrace> trace) {
        return new CompilationResult(schema, null, diagnostics, trace);
    }

    public static CompilationResult error(String message) {
        return new CompilationResult(null, message, List.of(), List.of());
    }

    public static CompilationResult error(String message, List<Diagnostic> diagnostics) {
        return new CompilationResult(null, message, diagnostics, List.of());
    }

    public static CompilationResult error(String message, List<Diagnostic> diagnostics, List<CompileTrace> trace) {
        return new CompilationResult(null, message, diagnostics, trace);
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

    public List<Diagnostic> getWarnings() {
        return warnings;
    }

    public List<Diagnostic> getErrors() {
        return errors;
    }

    public List<CompileTrace> getTrace() {
        return trace;
    }
}
