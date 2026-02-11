package ai.manifesto.compiler;

import ai.manifesto.compiler.diagnostics.Diagnostic;

import java.util.List;
import java.util.Map;

/**
 * KR: CompilePatchResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: CompilePatchResult is a result type carrying operation or execution outcomes.
 */
public record CompilePatchResult(
    List<Map<String, Object>> ops,
    List<Diagnostic> diagnostics
) {
}
