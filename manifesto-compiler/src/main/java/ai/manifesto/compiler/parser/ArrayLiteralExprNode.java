package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * ArrayLiteralExprNode - [1, 2, 3]
 */
public record ArrayLiteralExprNode(
    List<ExprNode> elements,
    SourceLocation location
) implements ExprNode {
}
