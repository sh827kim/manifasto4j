package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;

/**
 * ScopeAnalysisResult - scope 분석 결과
 */
public record ScopeAnalysisResult(
    Scope domainScope,
    List<Diagnostic> diagnostics
) {
}
