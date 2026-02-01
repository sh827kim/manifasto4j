package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * LiteralTypeNode - 리터럴 타입
 */
public record LiteralTypeNode(
    Object value,
    SourceLocation location
) implements TypeExprNode {
}
