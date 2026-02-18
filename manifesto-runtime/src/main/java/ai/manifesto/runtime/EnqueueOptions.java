package ai.manifesto.runtime;

/**
 * KR: EnqueueOptions는 큐 기반 실행에서 우선순위/중복 정책을 지정하는 옵션 계약입니다.
 * EN: EnqueueOptions defines priority and deduplication policy for queued execution.
 */
public record EnqueueOptions(
    int priority,
    boolean deduplicateByIntentType
) {
    public static EnqueueOptions defaults() {
        return new EnqueueOptions(0, false);
    }
}
