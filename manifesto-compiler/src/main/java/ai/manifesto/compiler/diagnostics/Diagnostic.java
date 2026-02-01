package ai.manifesto.compiler.diagnostics;

/**
 * Diagnostic - 컴파일 진단 정보
 */
public record Diagnostic(DiagnosticSeverity severity, DiagnosticCode code, String message, SourceSpan span) {
    public static Diagnostic error(DiagnosticCode code, String message, SourceSpan span) {
        return new Diagnostic(DiagnosticSeverity.ERROR, code, message, span);
    }

    public static Diagnostic warning(DiagnosticCode code, String message, SourceSpan span) {
        return new Diagnostic(DiagnosticSeverity.WARNING, code, message, span);
    }
}
