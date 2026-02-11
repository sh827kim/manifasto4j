package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * KR: Symbol는 컴파일러 분석 계층에서 전달되는 symbol 데이터를 담는 불변 레코드입니다.
 * EN: Symbol is an immutable record carrying symbol data in the compiler analyzer layer.
 */
public record Symbol(
    String name,
    SymbolKind kind,
    SourceLocation location
) {
}
