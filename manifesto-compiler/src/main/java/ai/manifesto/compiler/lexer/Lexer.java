package ai.manifesto.compiler.lexer;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticCode;
import ai.manifesto.compiler.diagnostics.SourceSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexer - MEL source to tokens.
 */
public final class Lexer {
    private static final Map<String, TokenKind> KEYWORDS = createKeywords();

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int lineStart = 0;

    public Lexer(String source) {
        this.source = source == null ? "" : source;
    }

    public LexResult tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        addToken(TokenKind.EOF, null);
        return new LexResult(List.copyOf(tokens), List.copyOf(diagnostics));
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenKind.LPAREN);
            case ')' -> addToken(TokenKind.RPAREN);
            case '{' -> addToken(TokenKind.LBRACE);
            case '}' -> addToken(TokenKind.RBRACE);
            case '[' -> addToken(TokenKind.LBRACKET);
            case ']' -> addToken(TokenKind.RBRACKET);
            case ',' -> addToken(TokenKind.COMMA);
            case ';' -> addToken(TokenKind.SEMICOLON);
            case '.' -> addToken(TokenKind.DOT);
            case '+' -> addToken(TokenKind.PLUS);
            case '-' -> addToken(TokenKind.MINUS);
            case '*' -> addToken(TokenKind.STAR);
            case '%' -> addToken(TokenKind.PERCENT);
            case ':' -> addToken(TokenKind.COLON);
            case '=' -> addToken(match('=') ? TokenKind.EQ_EQ : TokenKind.EQ);
            case '!' -> addToken(match('=') ? TokenKind.BANG_EQ : TokenKind.BANG);
            case '<' -> addToken(match('=') ? TokenKind.LT_EQ : TokenKind.LT);
            case '>' -> addToken(match('=') ? TokenKind.GT_EQ : TokenKind.GT);
            case '&' -> {
                if (match('&')) {
                    addToken(TokenKind.AMP_AMP);
                } else {
                    error("Expected '&&' for logical AND");
                }
            }
            case '|' -> addToken(match('|') ? TokenKind.PIPE_PIPE : TokenKind.PIPE);
            case '?' -> addToken(match('?') ? TokenKind.QUESTION_QUESTION : TokenKind.QUESTION);
            case '/' -> {
                if (match('/')) {
                    lineComment();
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(TokenKind.SLASH);
                }
            }
            case ' ', '\r', '\t' -> {
                // ignore whitespace
            }
            case '\n' -> newline();
            case '"' -> string('"');
            case '\'' -> string('\'');
            case '$' -> systemIdentifier();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Unexpected character '" + c + "'");
                }
            }
        }
    }

    private void lineComment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    private void blockComment() {
        int startLine = line;
        int startColumn = column - 2;
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance();
                advance();
                return;
            }
            if (peek() == '\n') {
                newline();
            }
            advance();
        }
        error("Unterminated block comment starting at line " + startLine + ":" + startColumn);
    }

    private void string(char quote) {
        int startLine = line;
        int startColumn = column - 1;
        StringBuilder value = new StringBuilder();

        while (!isAtEnd() && peek() != quote) {
            char c = advance();
            if (c == '\n') {
                error("Unterminated string literal");
                return;
            }
            if (c == '\\' && !isAtEnd()) {
                char next = advance();
                value.append(next);
                continue;
            }
            value.append(c);
        }

        if (isAtEnd()) {
            error("Unterminated string literal starting at line " + startLine + ":" + startColumn);
            return;
        }

        advance(); // closing quote
        addToken(TokenKind.STRING, value.toString());
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }

        String lexeme = source.substring(start, current);
        Object value;
        try {
            if (lexeme.contains(".")) {
                value = Double.parseDouble(lexeme);
            } else {
                value = Integer.parseInt(lexeme);
            }
        } catch (NumberFormatException e) {
            value = lexeme;
        }
        addToken(TokenKind.NUMBER, value);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenKind kind = KEYWORDS.get(text);
        if (kind == null) {
            kind = TokenKind.IDENTIFIER;
        }
        addToken(kind);
    }

    private void systemIdentifier() {
        while (isAlphaNumeric(peek()) || peek() == '.') {
            advance();
        }

        String lexeme = source.substring(start, current);
        TokenKind kind = "$item".equals(lexeme) ? TokenKind.ITEM : TokenKind.SYSTEM_IDENT;
        addToken(kind);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }
        current++;
        column++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private char advance() {
        char c = source.charAt(current++);
        column++;
        return c;
    }

    private void newline() {
        line++;
        column = 1;
        lineStart = current;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenKind kind) {
        addToken(kind, null);
    }

    private void addToken(TokenKind kind, Object value) {
        String lexeme = source.substring(start, current);
        SourceLocation location = currentLocation();
        tokens.add(Token.of(kind, lexeme, location, value));
    }

    private SourceLocation currentLocation() {
        int startLine = line;
        int startColumn = start - lineStart + 1;
        SourcePosition startPos = SourcePosition.of(startLine, startColumn, start);
        SourcePosition endPos = SourcePosition.of(line, column, current);
        return SourceLocation.of(startPos, endPos);
    }

    private void error(String message) {
        int length = Math.max(1, current - start);
        diagnostics.add(Diagnostic.error(
            DiagnosticCode.MEL_LEXER,
            message,
            SourceSpan.of(line, Math.max(1, column - length), length)
        ));
    }

    private static Map<String, TokenKind> createKeywords() {
        Map<String, TokenKind> keywords = new HashMap<>();
        keywords.put("domain", TokenKind.DOMAIN);
        keywords.put("state", TokenKind.STATE);
        keywords.put("computed", TokenKind.COMPUTED);
        keywords.put("action", TokenKind.ACTION);
        keywords.put("effect", TokenKind.EFFECT);
        keywords.put("when", TokenKind.WHEN);
        keywords.put("once", TokenKind.ONCE);
        keywords.put("patch", TokenKind.PATCH);
        keywords.put("unset", TokenKind.UNSET);
        keywords.put("merge", TokenKind.MERGE);
        keywords.put("true", TokenKind.TRUE);
        keywords.put("false", TokenKind.FALSE);
        keywords.put("null", TokenKind.NULL);
        keywords.put("as", TokenKind.AS);
        keywords.put("available", TokenKind.AVAILABLE);
        keywords.put("fail", TokenKind.FAIL);
        keywords.put("stop", TokenKind.STOP);
        keywords.put("with", TokenKind.WITH);
        keywords.put("type", TokenKind.TYPE);
        keywords.put("import", TokenKind.IMPORT);
        keywords.put("from", TokenKind.FROM);
        keywords.put("export", TokenKind.EXPORT);
        return keywords;
    }

    public record LexResult(List<Token> tokens, List<Diagnostic> diagnostics) {}
}
