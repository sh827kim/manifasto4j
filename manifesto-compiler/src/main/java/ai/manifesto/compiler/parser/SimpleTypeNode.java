package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * SimpleTypeNode - 기본 타입
 */
public record SimpleTypeNode(
    String name,
    SourceLocation location
) implements TypeExprNode {
}
