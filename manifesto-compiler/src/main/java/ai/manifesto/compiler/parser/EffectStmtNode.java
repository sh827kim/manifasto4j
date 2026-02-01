package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * EffectStmtNode - effect statement
 */
public record EffectStmtNode(
    String effectType,
    List<EffectArgNode> args,
    SourceLocation location
) implements InnerStmtNode {
}
