package ai.manifesto.host.runtime;

/**
 * KR: Host 실행 파이프라인에서 처리되는 job 유형 목록입니다.
 * EN: Job type list processed by the host execution pipeline.
 */
public enum HostJobType {
    START_INTENT,
    CONTINUE_COMPUTE,
    FULFILL_REQUIREMENTS
}
