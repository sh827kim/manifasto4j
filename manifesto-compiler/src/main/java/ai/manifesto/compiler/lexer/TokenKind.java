package ai.manifesto.compiler.lexer;

/**
 * TokenKind - MEL lexer token kinds (TS compiler parity)
 */
public enum TokenKind {
    // Keywords
    DOMAIN,
    STATE,
    COMPUTED,
    ACTION,
    EFFECT,
    WHEN,
    ONCE,
    PATCH,
    UNSET,
    MERGE,
    TRUE,
    FALSE,
    NULL,
    AS,
    AVAILABLE,
    FAIL,
    STOP,
    WITH,
    TYPE,
    IMPORT,
    FROM,
    EXPORT,

    // Operators
    PLUS,
    MINUS,
    STAR,
    SLASH,
    PERCENT,
    EQ_EQ,
    BANG_EQ,
    LT,
    LT_EQ,
    GT,
    GT_EQ,
    AMP_AMP,
    PIPE_PIPE,
    BANG,
    QUESTION_QUESTION,
    QUESTION,
    COLON,
    EQ,

    // Delimiters
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,
    COMMA,
    SEMICOLON,
    DOT,
    PIPE,

    // Literals
    NUMBER,
    STRING,
    IDENTIFIER,

    // System identifiers
    SYSTEM_IDENT,
    ITEM,

    // Special
    EOF,
    ERROR
}
