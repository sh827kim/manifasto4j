package ai.manifesto.sdk;

/**
 * KR: 액션 완료 결과입니다.
 * EN: Action completed result.
 */
public record CompletedActionResult(
    String status,
    String worldId,
    RuntimeKind runtimeKind
) implements ActionResult {
}
