package ai.manifesto.compiler;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.core.schema.DomainSchema;

import java.util.List;

/**
 * KR: GenerateResult는 연산/실행 결과를 전달하는 결과 타입입니다.
 * EN: GenerateResult is a result type carrying operation or execution outcomes.
 */
public record GenerateResult(
    DomainSchema schema,
    List<Diagnostic> diagnostics
) {
}
