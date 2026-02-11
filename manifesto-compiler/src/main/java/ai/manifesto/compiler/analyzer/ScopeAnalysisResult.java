package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;

/**
 * KR: ScopeAnalysisResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: ScopeAnalysisResult is a result type carrying operation or execution outcomes.
 */
public record ScopeAnalysisResult(
    Scope domainScope,
    List<Diagnostic> diagnostics
) {
}
