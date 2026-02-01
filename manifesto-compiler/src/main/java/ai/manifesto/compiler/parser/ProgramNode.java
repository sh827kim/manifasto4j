package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * ProgramNode - MEL 프로그램 루트
 */
public record ProgramNode(
    List<ImportNode> imports,
    DomainNode domain,
    SourceLocation location
) implements AstNode {
}
