package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * RecordTypeNode - record 타입
 */
public record RecordTypeNode(
    TypeExprNode keyType,
    TypeExprNode valueType,
    SourceLocation location
) implements TypeExprNode {
}
