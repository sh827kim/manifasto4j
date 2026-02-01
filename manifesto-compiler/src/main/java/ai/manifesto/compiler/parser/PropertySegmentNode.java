package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * PropertySegmentNode - path property segment
 */
public record PropertySegmentNode(
    String name,
    SourceLocation location
) implements PathSegmentNode {
}
