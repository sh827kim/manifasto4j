package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * IdentifierExprNode - identifier expression
 */
public record IdentifierExprNode(
    String name,
    SourceLocation location
) implements ExprNode {
}
