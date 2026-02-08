package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * OnceIntentStmtNode - onceIntent guard
 */
public record OnceIntentStmtNode(
    ExprNode condition,
    List<InnerStmtNode> body,
    SourceLocation location
) implements GuardedStmtNode, InnerStmtNode {
}
