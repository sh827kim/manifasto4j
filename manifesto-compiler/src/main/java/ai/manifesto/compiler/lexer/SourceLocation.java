package ai.manifesto.compiler.lexer;

/**
 * SourceLocation - start/end positions for a token.
 */
public record SourceLocation(SourcePosition start, SourcePosition end) {
    public static SourceLocation of(SourcePosition start, SourcePosition end) {
        return new SourceLocation(start, end);
    }
}
