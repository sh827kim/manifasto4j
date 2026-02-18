package ai.manifesto.runtime;

/**
 * KR: 권한 정책 등으로 액션이 거절된 결과입니다.
 * EN: Result for an action rejected by authority or policy.
 */
public record RejectedActionResult(
    String status,
    String reason,
    RuntimeKind runtimeKind
) implements ActionResult {
    public RejectedActionResult(String reason, RuntimeKind runtimeKind) {
        this("rejected", reason, runtimeKind);
    }
}
