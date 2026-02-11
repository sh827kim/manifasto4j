package ai.manifesto.compiler.diagnostics;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: DiagnosticsSink는 컴파일러 진단 계층에서 diagnostics sink 역할을 수행하는 구현 타입입니다.
 * EN: DiagnosticsSink is an implementation type performing diagnostics sink roles in the compiler diagnostics layer.
 */
public final class DiagnosticsSink {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void report(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public List<Diagnostic> getDiagnostics() {
        return List.copyOf(diagnostics);
    }
}
