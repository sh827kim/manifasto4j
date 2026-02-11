package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;

/**
 * KR: ValidationResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: ValidationResult is a result type carrying operation or execution outcomes.
 */
public record ValidationResult(
    boolean valid,
    List<Diagnostic> diagnostics
) {
}
