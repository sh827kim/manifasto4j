package ai.manifesto.compiler.diagnostics;

/**
 * DiagnosticCode - MEL 진단 코드 (TS와 동일한 코드 문자열 유지)
 */
public enum DiagnosticCode {
    // Syntax / semantic (E0xx)
    E001("E001", "semantic", "$system.* cannot be used in computed expressions (non-deterministic)"),
    E002("E002", "semantic", "$system.* cannot be used in state initializers"),
    E003("E003", "semantic", "Invalid $system reference"),
    E004("E004", "syntax", "Identifier starts with reserved prefix '__sys__'"),
    E005("E005", "semantic", "available expression must be pure (no Effects, no $system.*)"),
    E006("E006", "semantic", "fail must be inside a guard (when or once)"),
    E007("E007", "semantic", "stop must be inside a guard (when or once)"),
    E008("E008", "semantic", "stop message suggests waiting/pending - use 'Already processed' style instead"),
    E009("E009", "semantic", "Primitive aggregation (sum, min, max) only allowed in computed"),
    E010("E010", "semantic", "Primitive aggregation does not allow composition - use direct reference only"),
    E011("E011", "semantic", "reduce/fold/scan is forbidden - use sum, min, max for aggregation"),

    // Scope (E1xx)
    E_UNDEFINED("E_UNDEFINED", "semantic", "Undefined identifier"),
    E_DUPLICATE("E_DUPLICATE", "semantic", "Duplicate identifier"),
    E_INVALID_ACCESS("E_INVALID_ACCESS", "semantic", "Invalid access to identifier in this context"),

    // Statements (E2xx)
    E_UNGUARDED_STMT("E_UNGUARDED_STMT", "semantic", "Statement must be inside a guard (when or once)"),
    E_UNGUARDED_PATCH("E_UNGUARDED_PATCH", "semantic", "Patch must be inside a guard"),
    E_UNGUARDED_EFFECT("E_UNGUARDED_EFFECT", "semantic", "Effect must be inside a guard"),

    // Types (E3xx)
    E_ARG_COUNT("E_ARG_COUNT", "type", "Wrong number of arguments"),
    E_TYPE_MISMATCH("E_TYPE_MISMATCH", "type", "Type mismatch"),

    // Warnings (W0xx)
    W_NON_BOOL_COND("W_NON_BOOL_COND", "semantic", "Condition may not be boolean"),
    W_UNUSED("W_UNUSED", "semantic", "Unused identifier"),
    W012("W012", "type", "Anonymous object type in state field - use named type declaration instead"),

    // Lexer/Parser
    MEL_LEXER("MEL_LEXER", "syntax", "Lexer error"),
    MEL_PARSER("MEL_PARSER", "syntax", "Parser error");

    private final String code;
    private final String category;
    private final String defaultMessage;

    DiagnosticCode(String code, String category, String defaultMessage) {
        this.code = code;
        this.category = category;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String category() {
        return category;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
