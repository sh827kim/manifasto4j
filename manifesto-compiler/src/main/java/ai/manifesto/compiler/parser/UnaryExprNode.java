package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * UnaryExprNode - !a, -a
 */
public record UnaryExprNode(
    String operator,
    ExprNode operand,
    SourceLocation location
) implements ExprNode {
}
