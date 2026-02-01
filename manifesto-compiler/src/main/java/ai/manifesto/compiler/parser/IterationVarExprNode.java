package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * IterationVarExprNode - $item
 */
public record IterationVarExprNode(
    String name,
    SourceLocation location
) implements ExprNode {
}
