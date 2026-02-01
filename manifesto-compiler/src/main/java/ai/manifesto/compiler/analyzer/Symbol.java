package ai.manifesto.compiler.analyzer;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * Symbol - scope symbol entry
 */
public record Symbol(
    String name,
    SymbolKind kind,
    SourceLocation location
) {
}
