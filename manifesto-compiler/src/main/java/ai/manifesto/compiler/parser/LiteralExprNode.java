package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * LiteralExprNode - literal expression
 */
public record LiteralExprNode(
    Object value,
    String literalType,
    SourceLocation location
) implements ExprNode {
}
