package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * ArrayTypeNode - 배열 타입
 */
public record ArrayTypeNode(
    TypeExprNode elementType,
    SourceLocation location
) implements TypeExprNode {
}
