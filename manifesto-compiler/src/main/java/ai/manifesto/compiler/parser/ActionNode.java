package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * ActionNode - action 선언
 */
public record ActionNode(
    String name,
    List<ParamNode> params,
    ExprNode available,
    List<GuardedStmtNode> body,
    SourceLocation location
) implements DomainMember {
}
