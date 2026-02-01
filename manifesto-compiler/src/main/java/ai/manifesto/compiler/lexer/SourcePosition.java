package ai.manifesto.compiler.lexer;

/**
 * SourcePosition - line/column/offset location.
 */
public record SourcePosition(int line, int column, int offset) {
    public static SourcePosition of(int line, int column, int offset) {
        return new SourcePosition(line, column, offset);
    }
}
