package ai.manifesto.compiler.diagnostics;

/**
 * KR: Diagnostic는 컴파일러 진단 계층에서 전달되는 diagnostic 데이터를 담는 불변 레코드입니다.
 * EN: Diagnostic is an immutable record carrying diagnostic data in the compiler diagnostics layer.
 */
public record Diagnostic(DiagnosticSeverity severity, DiagnosticCode code, String message, SourceSpan span) {
    public static Diagnostic error(DiagnosticCode code, String message, SourceSpan span) {
        return new Diagnostic(DiagnosticSeverity.ERROR, code, message, span);
    }

    public static Diagnostic warning(DiagnosticCode code, String message, SourceSpan span) {
        return new Diagnostic(DiagnosticSeverity.WARNING, code, message, span);
    }
}
