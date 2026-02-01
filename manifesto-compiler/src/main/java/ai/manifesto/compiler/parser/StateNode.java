package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * StateNode - state 블록
 */
public record StateNode(
    List<StateFieldNode> fields,
    SourceLocation location
) implements DomainMember {
}
