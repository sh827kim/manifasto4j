package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * ObjectPropertyNode - object literal field
 */
public record ObjectPropertyNode(
    String key,
    ExprNode value,
    SourceLocation location
) implements AstNode {
}
