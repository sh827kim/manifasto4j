package ai.manifesto.compiler.lexer;

/**
 * KR: SourcePosition는 컴파일러 렉서 계층에서 전달되는 source position 데이터를 담는 불변 레코드입니다.
 * EN: SourcePosition is an immutable record carrying source position data in the compiler lexer layer.
 */
public record SourcePosition(int line, int column, int offset) {
    public static SourcePosition of(int line, int column, int offset) {
        return new SourcePosition(line, column, offset);
    }
}
