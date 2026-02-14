package ai.manifesto.translator.targets.manifesto;

/**
 * KR: Manifesto lowering 실패 종류입니다.
 * EN: Failure kinds for Manifesto lowering.
 */
public enum LoweringFailureKind {
    UNRESOLVED_ACTION,
    LEXICON_REJECTED,
    RESOLVER_FAILURE,
    LOWERER_NULL_RESULT,
    LOWERER_EXCEPTION,
    UNSUPPORTED_EVENT,
    INVALID_ARGS,
    MISSING_REQUIRED,
    SCHEMA_MISMATCH,
    LOSSY_LOWERING,
    INTERNAL_ERROR
}
