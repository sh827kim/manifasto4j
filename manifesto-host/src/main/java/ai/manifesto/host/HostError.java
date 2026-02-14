package ai.manifesto.host;

import java.util.Map;

/**
 * KR: Host 실행 실패를 표현하는 공통 에러 모델입니다.
 * EN: Common error model representing Host execution failures.
 */
public record HostError(
    HostErrorCode code,
    String message,
    Map<String, Object> details
) {
    public static HostError of(HostErrorCode code, String message) {
        return new HostError(code, message, Map.of());
    }

    public static HostError of(HostErrorCode code, String message, Map<String, Object> details) {
        return new HostError(code, message, details == null ? Map.of() : Map.copyOf(details));
    }
}
