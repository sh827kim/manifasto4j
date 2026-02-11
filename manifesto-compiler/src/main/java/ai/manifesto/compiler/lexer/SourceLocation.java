package ai.manifesto.compiler.lexer;

/**
 * KR: SourceLocation는 컴파일러 렉서 계층에서 전달되는 source location 데이터를 담는 불변 레코드입니다.
 * EN: SourceLocation is an immutable record carrying source location data in the compiler lexer layer.
 */
public record SourceLocation(SourcePosition start, SourcePosition end) {
    public static SourceLocation of(SourcePosition start, SourcePosition end) {
        return new SourceLocation(start, end);
    }
}
