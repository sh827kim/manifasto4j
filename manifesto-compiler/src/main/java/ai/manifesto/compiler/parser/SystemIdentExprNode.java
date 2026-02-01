package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * SystemIdentExprNode - $system/$meta/$input path
 */
public record SystemIdentExprNode(
    List<String> path,
    SourceLocation location
) implements ExprNode {
}
