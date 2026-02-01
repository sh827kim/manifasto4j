package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.lexer.TokenKind;

/**
 * Precedence - MEL operator precedence
 */
public enum Precedence {
    NONE(0),
    TERNARY(1),
    NULLISH(2),
    OR(3),
    AND(4),
    EQUALITY(5),
    COMPARISON(6),
    ADDITIVE(7),
    MULTIPLICATIVE(8),
    UNARY(9),
    CALL(10),
    ACCESS(11);

    private final int level;

    Precedence(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public static Precedence getBinaryPrecedence(TokenKind kind) {
        return switch (kind) {
            case QUESTION -> TERNARY;
            case QUESTION_QUESTION -> NULLISH;
            case PIPE_PIPE -> OR;
            case AMP_AMP -> AND;
            case EQ_EQ, BANG_EQ -> EQUALITY;
            case LT, LT_EQ, GT, GT_EQ -> COMPARISON;
            case PLUS, MINUS -> ADDITIVE;
            case STAR, SLASH, PERCENT -> MULTIPLICATIVE;
            default -> NONE;
        };
    }

    public static String tokenToBinaryOp(TokenKind kind) {
        return switch (kind) {
            case PLUS -> "+";
            case MINUS -> "-";
            case STAR -> "*";
            case SLASH -> "/";
            case PERCENT -> "%";
            case EQ_EQ -> "==";
            case BANG_EQ -> "!=";
            case LT -> "<";
            case LT_EQ -> "<=";
            case GT -> ">";
            case GT_EQ -> ">=";
            case AMP_AMP -> "&&";
            case PIPE_PIPE -> "||";
            case QUESTION_QUESTION -> "??";
            default -> null;
        };
    }

    public static boolean isRightAssociative(TokenKind kind) {
        return kind == TokenKind.QUESTION || kind == TokenKind.QUESTION_QUESTION;
    }
}
