package ai.manifesto.sdk;

/**
 * KR: 정책 거절 결과입니다.
 * EN: Action rejected result.
 */
public record RejectedActionResult(
    String status,
    String reason,
    RuntimeKind runtimeKind
) implements ActionResult {
}
