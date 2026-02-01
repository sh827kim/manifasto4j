package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * ParamNode - action 파라미터
 */
public record ParamNode(
    String name,
    TypeExprNode typeExpr,
    SourceLocation location
) implements AstNode {
}
