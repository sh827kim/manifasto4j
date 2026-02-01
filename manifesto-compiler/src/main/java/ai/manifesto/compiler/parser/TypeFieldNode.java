package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * TypeFieldNode - object type field
 */
public record TypeFieldNode(
    String name,
    TypeExprNode typeExpr,
    boolean optional,
    SourceLocation location
) implements AstNode {
}
