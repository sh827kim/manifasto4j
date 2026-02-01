package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * TernaryExprNode - a ? b : c
 */
public record TernaryExprNode(
    ExprNode condition,
    ExprNode consequent,
    ExprNode alternate,
    SourceLocation location
) implements ExprNode {
}
