package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * StateFieldNode - state field 선언
 */
public record StateFieldNode(
    String name,
    TypeExprNode typeExpr,
    ExprNode initializer,
    SourceLocation location
) implements AstNode {
}
