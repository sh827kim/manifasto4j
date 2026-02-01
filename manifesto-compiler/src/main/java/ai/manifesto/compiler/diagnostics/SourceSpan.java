package ai.manifesto.compiler.diagnostics;

/**
 * SourceSpan - 소스 위치 범위
 */
public record SourceSpan(int line, int column, int length) {
    public static SourceSpan of(int line, int column, int length) {
        return new SourceSpan(line, column, length);
    }
}
