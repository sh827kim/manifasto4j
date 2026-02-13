package ai.manifesto.host.runtime;

/**
 * KR: HostRuntimeTraceEvent는 Host runner/mailbox 실행 중 발생한 추적 이벤트를 표현하는 레코드입니다.
 * EN: HostRuntimeTraceEvent is an immutable record representing trace events produced during Host runner/mailbox execution.
 */
public record HostRuntimeTraceEvent(
    String type,
    String executionKey,
    String jobType,
    Boolean queueEmpty,
    Boolean kickRequested,
    Long timestamp
) {
}
