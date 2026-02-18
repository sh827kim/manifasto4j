package ai.manifesto.app;

/**
 * KR: 액션 실행이 정상 완료된 결과입니다.
 * EN: Result for a successfully completed action execution.
 */
public record CompletedActionResult(
    String status,
    String worldId,
    RuntimeKind runtimeKind
) implements ActionResult {
    public CompletedActionResult(String worldId, RuntimeKind runtimeKind) {
        this("completed", worldId, runtimeKind);
    }
}
