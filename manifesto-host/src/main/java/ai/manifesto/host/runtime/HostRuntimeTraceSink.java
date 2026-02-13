package ai.manifesto.host.runtime;

/**
 * KR: HostRuntimeTraceSink는 Host 런타임 추적 이벤트를 수신하는 출력 포트 인터페이스입니다.
 * EN: HostRuntimeTraceSink is an output-port interface that consumes Host runtime trace events.
 */
@FunctionalInterface
public interface HostRuntimeTraceSink {
    HostRuntimeTraceSink NOOP = event -> { };

    void onEvent(HostRuntimeTraceEvent event);
}
