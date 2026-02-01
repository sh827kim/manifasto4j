package ai.manifesto.compiler.diagnostics;

import java.util.ArrayList;
import java.util.List;

/**
 * DiagnosticsSink - 진단 수집기
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
