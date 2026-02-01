package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * PropertyAccessExprNode - a.b
 */
public record PropertyAccessExprNode(
    ExprNode object,
    String property,
    SourceLocation location
) implements ExprNode {
}
