package ai.manifesto.app;

/**
 * KR: App 모듈 도메인 에러의 기본 타입입니다.
 * EN: Base type for domain errors in the App module.
 */
public class ManifestoAppException extends RuntimeException {
    private final String code;

    public ManifestoAppException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
