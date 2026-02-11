package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;

/**
 * KR: ParseResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: ParseResult is a result type carrying operation or execution outcomes.
 */
public record ParseResult(
    ProgramNode program,
    List<Diagnostic> diagnostics
) {
}
