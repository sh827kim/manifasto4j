package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * PathNode - patch target path
 */
public record PathNode(
    List<PathSegmentNode> segments,
    SourceLocation location
) implements AstNode {
}
