package ai.manifesto.sdk;

/**
 * KR: 액션 실패 결과입니다.
 * EN: Action failed result.
 */
public record FailedActionResult(
    String status,
    String reason,
    String worldId,
    RuntimeKind runtimeKind
) implements ActionResult {
}
