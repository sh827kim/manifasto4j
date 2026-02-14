package ai.manifesto.app;

/**
 * KR: 실행 준비 단계에서 실패한 결과입니다.
 * EN: Result for a failure during action preparation.
 */
public record PreparationFailedActionResult(
    String status,
    String reason,
    RuntimeKind runtimeKind
) implements ActionResult {
    public PreparationFailedActionResult(String reason, RuntimeKind runtimeKind) {
        this("preparation_failed", reason, runtimeKind);
    }
}
