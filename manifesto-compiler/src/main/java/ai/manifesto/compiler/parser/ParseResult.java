package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;

/**
 * ParseResult - MEL parse 결과
 */
public record ParseResult(
    ProgramNode program,
    List<Diagnostic> diagnostics
) {
}
