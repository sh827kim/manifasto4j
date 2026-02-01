package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.SourceLocation;

import java.util.List;

/**
 * ImportNode - import { a, b } from "..."
 */
public record ImportNode(
    List<String> names,
    String from,
    SourceLocation location
) implements AstNode {
}
