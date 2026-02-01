package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * StopStmtNode - stop statement
 */
public record StopStmtNode(
    String reason,
    SourceLocation location
) implements InnerStmtNode {
}
