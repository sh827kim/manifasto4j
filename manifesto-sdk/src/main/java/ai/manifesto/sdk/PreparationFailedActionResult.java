package ai.manifesto.sdk;

/**
 * KR: 준비 단계 실패 결과입니다.
 * EN: Action preparation failed result.
 */
public record PreparationFailedActionResult(
    String status,
    String reason,
    RuntimeKind runtimeKind
) implements ActionResult {
}
