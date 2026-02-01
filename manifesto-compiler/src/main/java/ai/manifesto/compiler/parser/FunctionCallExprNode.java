package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * FunctionCallExprNode - function call
 */
public record FunctionCallExprNode(
    String name,
    List<ExprNode> args,
    SourceLocation location
) implements ExprNode {
}
