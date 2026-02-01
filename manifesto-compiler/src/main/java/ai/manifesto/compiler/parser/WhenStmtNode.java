package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * WhenStmtNode - when guard
 */
public record WhenStmtNode(
    ExprNode condition,
    List<InnerStmtNode> body,
    SourceLocation location
) implements GuardedStmtNode, InnerStmtNode {
}
