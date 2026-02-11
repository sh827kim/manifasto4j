package ai.manifesto.compiler;

/**
 * KR: LoweringError는 실패 원인과 관련 메타데이터를 표현하는 오류 타입입니다.
 * EN: LoweringError is an error type representing failure cause and related metadata.
 */
public class LoweringError extends RuntimeException {
    private final LoweringErrorCode code;

    public LoweringError(LoweringErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public LoweringErrorCode getCode() {
        return code;
    }

    public static LoweringError invalidKindForContext(String kind, String mode) {
        return new LoweringError(LoweringErrorCode.INVALID_KIND_FOR_CONTEXT,
            "Invalid kind '" + kind + "' for context: " + mode);
    }

    public static LoweringError invalidSysPath(String path) {
        return new LoweringError(LoweringErrorCode.INVALID_SYS_PATH,
            "Invalid sys path: " + path);
    }

    public static LoweringError unknownCallFn(String fn) {
        return new LoweringError(LoweringErrorCode.UNKNOWN_CALL_FN,
            "Unknown call fn: " + fn);
    }

    public static LoweringError unsupportedBase(String kind) {
        return new LoweringError(LoweringErrorCode.UNSUPPORTED_BASE,
            "Unsupported base kind: " + kind);
    }

    public static LoweringError invalidShape(String message) {
        return new LoweringError(LoweringErrorCode.INVALID_SHAPE, message);
    }

    public static LoweringError unknownNodeKind(String kind) {
        return new LoweringError(LoweringErrorCode.UNKNOWN_NODE_KIND,
            "Unknown node kind: " + kind);
    }
}
