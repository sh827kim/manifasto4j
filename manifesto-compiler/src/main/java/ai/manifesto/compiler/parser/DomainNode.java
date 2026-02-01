package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * DomainNode - domain 선언
 */
public record DomainNode(
    String name,
    List<TypeDeclNode> types,
    List<DomainMember> members,
    SourceLocation location
) implements AstNode {
}
