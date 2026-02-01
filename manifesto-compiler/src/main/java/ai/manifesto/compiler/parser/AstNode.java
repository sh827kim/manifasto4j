package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * AstNode - MEL AST 공통 노드
 */
public interface AstNode {
    SourceLocation location();
}
