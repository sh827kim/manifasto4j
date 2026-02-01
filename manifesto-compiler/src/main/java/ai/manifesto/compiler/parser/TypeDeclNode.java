package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * TypeDeclNode - type Name = TypeExpr
 */
public record TypeDeclNode(
    String name,
    TypeExprNode typeExpr,
    SourceLocation location
) implements AstNode {
}
