package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * IndexSegmentNode - path index segment
 */
public record IndexSegmentNode(
    ExprNode index,
    SourceLocation location
) implements PathSegmentNode {
}
