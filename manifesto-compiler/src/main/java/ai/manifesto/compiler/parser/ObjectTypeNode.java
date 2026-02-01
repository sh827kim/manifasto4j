package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * ObjectTypeNode - inline object type
 */
public record ObjectTypeNode(
    List<TypeFieldNode> fields,
    SourceLocation location
) implements TypeExprNode {
}
