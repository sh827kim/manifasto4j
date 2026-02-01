package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * ComputedNode - computed 선언
 */
public record ComputedNode(
    String name,
    ExprNode expression,
    SourceLocation location
) implements DomainMember {
}
