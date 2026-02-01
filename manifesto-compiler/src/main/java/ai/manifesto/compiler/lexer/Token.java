package ai.manifesto.compiler.lexer;

/**
 * Token - lexical token
 */
public record Token(TokenKind kind, String lexeme, Object value, SourceLocation location) {
    public static Token of(TokenKind kind, String lexeme, SourceLocation location, Object value) {
        return new Token(kind, lexeme, value, location);
    }
}
