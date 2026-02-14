package ai.manifesto.translator.adapters.spi;

/**
 * KR: LLM provider 호출 실패 예외입니다.
 * EN: Exception raised when LLM provider calls fail.
 */
public final class LlmException extends RuntimeException {
    private final LlmErrorCode code;
    private final boolean retryable;

    public LlmException(String message, LlmErrorCode code, boolean retryable) {
        super(message);
        this.code = code == null ? LlmErrorCode.UNKNOWN : code;
        this.retryable = retryable;
    }

    public LlmException(String message, LlmErrorCode code, boolean retryable, Throwable cause) {
        super(message, cause);
        this.code = code == null ? LlmErrorCode.UNKNOWN : code;
        this.retryable = retryable;
    }

    public LlmErrorCode code() {
        return code;
    }

    public boolean retryable() {
        return retryable;
    }
}
