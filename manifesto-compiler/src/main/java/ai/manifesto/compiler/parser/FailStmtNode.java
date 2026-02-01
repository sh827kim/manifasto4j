package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * FailStmtNode - fail statement
 */
public record FailStmtNode(
    String code,
    ExprNode message,
    SourceLocation location
) implements InnerStmtNode {
}
