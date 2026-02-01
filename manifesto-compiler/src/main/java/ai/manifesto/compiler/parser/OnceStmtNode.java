package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * OnceStmtNode - once guard
 */
public record OnceStmtNode(
    PathNode marker,
    ExprNode condition,
    List<InnerStmtNode> body,
    SourceLocation location
) implements GuardedStmtNode, InnerStmtNode {
}
