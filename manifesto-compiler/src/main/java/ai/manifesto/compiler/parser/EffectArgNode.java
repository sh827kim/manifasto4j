package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * EffectArgNode - effect argument
 */
public record EffectArgNode(
    String name,
    AstNode value,
    boolean isPath,
    SourceLocation location
) implements AstNode {
}
