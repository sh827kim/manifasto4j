package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * IndexAccessExprNode - a[b]
 */
public record IndexAccessExprNode(
    ExprNode object,
    ExprNode index,
    SourceLocation location
) implements ExprNode {
}
