package ai.manifesto.compiler.analyzer;

/**
 * KR: SymbolKind는 컴파일러 분석 계층에서 사용하는 symbol kind 분류 값을 열거합니다.
 * EN: SymbolKind enumerates symbol kind classification values used in the compiler analyzer layer.
 */
public enum SymbolKind {
    STATE,
    COMPUTED,
    PARAM,
    ACTION,
    ITERATION
}
