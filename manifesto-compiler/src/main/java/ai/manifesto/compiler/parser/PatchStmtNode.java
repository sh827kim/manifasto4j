package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

/**
 * PatchStmtNode - patch statement
 */
public record PatchStmtNode(
    PathNode path,
    String op,
    ExprNode value,
    SourceLocation location
) implements InnerStmtNode {
}
