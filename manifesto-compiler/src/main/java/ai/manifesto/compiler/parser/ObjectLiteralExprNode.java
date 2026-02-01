package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * ObjectLiteralExprNode - { a: 1 }
 */
public record ObjectLiteralExprNode(
    List<ObjectPropertyNode> properties,
    SourceLocation location
) implements ExprNode {
}
