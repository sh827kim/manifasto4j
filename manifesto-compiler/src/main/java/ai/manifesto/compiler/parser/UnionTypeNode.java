package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * UnionTypeNode - union 타입
 */
public record UnionTypeNode(
    List<TypeExprNode> types,
    SourceLocation location
) implements TypeExprNode {
}
