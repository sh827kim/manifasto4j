package ai.manifesto.runtime;

/**
 * KR: 액션 실행이 실패한 결과입니다.
 * EN: Result for a failed action execution.
 */
public record FailedActionResult(
    String status,
    String reason,
    String worldId,
    RuntimeKind runtimeKind
) implements ActionResult {
    public FailedActionResult(String reason, String worldId, RuntimeKind runtimeKind) {
        this("failed", reason, worldId, runtimeKind);
    }
}
