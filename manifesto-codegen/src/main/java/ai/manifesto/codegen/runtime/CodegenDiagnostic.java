package ai.manifesto.codegen.runtime;

/**
 * KR: codegen 실행 진단 메시지입니다.
 * EN: Diagnostic message from codegen execution.
 */
public record CodegenDiagnostic(
    CodegenDiagnosticLevel level,
    String plugin,
    String message
) {
    public static CodegenDiagnostic error(String plugin, String message) {
        return new CodegenDiagnostic(CodegenDiagnosticLevel.ERROR, plugin, message);
    }

    public static CodegenDiagnostic warn(String plugin, String message) {
        return new CodegenDiagnostic(CodegenDiagnosticLevel.WARN, plugin, message);
    }
}
