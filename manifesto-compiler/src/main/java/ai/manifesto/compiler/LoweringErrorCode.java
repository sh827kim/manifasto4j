package ai.manifesto.compiler;

/**
 * KR: LoweringErrorCode는 컴파일러 모듈에서 사용하는 lowering error code 분류 값을 열거합니다.
 * EN: LoweringErrorCode enumerates lowering error code classification values used in the compiler module.
 */
public enum LoweringErrorCode {
    INVALID_KIND_FOR_CONTEXT,
    INVALID_SYS_PATH,
    UNKNOWN_CALL_FN,
    UNSUPPORTED_BASE,
    INVALID_SHAPE,
    UNKNOWN_NODE_KIND
}
