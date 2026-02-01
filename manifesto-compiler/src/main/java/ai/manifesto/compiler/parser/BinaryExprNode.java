package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * BinaryExprNode - a + b
 */
public record BinaryExprNode(
    String operator,
    ExprNode left,
    ExprNode right,
    SourceLocation location
) implements ExprNode {
}
