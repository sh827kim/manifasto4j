package ai.manifesto.compiler.diagnostics;

/**
 * KR: SourceSpan는 컴파일러 진단 계층에서 전달되는 source span 데이터를 담는 불변 레코드입니다.
 * EN: SourceSpan is an immutable record carrying source span data in the compiler diagnostics layer.
 */
public record SourceSpan(int line, int column, int length) {
    public static SourceSpan of(int line, int column, int length) {
        return new SourceSpan(line, column, length);
    }
}
