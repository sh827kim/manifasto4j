package ai.manifesto.compiler.lexer;

/**
 * KR: Token는 컴파일러 렉서 계층에서 전달되는 token 데이터를 담는 불변 레코드입니다.
 * EN: Token is an immutable record carrying token data in the compiler lexer layer.
 */
public record Token(TokenKind kind, String lexeme, Object value, SourceLocation location) {
    public static Token of(TokenKind kind, String lexeme, SourceLocation location, Object value) {
        return new Token(kind, lexeme, value, location);
    }
}
