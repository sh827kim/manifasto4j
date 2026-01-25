package ai.manifesto.core.trace;

/**
 * TraceContext - 추적 정보 컨텍스트
 *
 * Flow와 Expression 평가 중에 발생하는 모든 이벤트를 기록한다.
 * 결정론적 ID 생성을 위해 카운터를 유지한다.
 */
public class TraceContext {
    private final long timestamp;
    private int index; // mutable: 결정론적 ID 생성용

    private TraceContext(long timestamp) {
        this.timestamp = timestamp;
        this.index = 0;
    }

    /**
     * 추적 컨텍스트 생성
     */
    public static TraceContext create(long timestamp) {
        return new TraceContext(timestamp);
    }

    /**
     * 타임스탐프 반환
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 다음 추적 노드 ID 반환 (결정론적)
     * 같은 intentId + counter -> 같은 ID
     */
    public String nextId() {
        return "trace_" + (index++);
    }

    /**
     * 현재 인덱스 반환
     */
    public int getCurrentIndex() {
        return index;
    }

    /**
     * 인덱스 초기화 (테스트용)
     */
    public void resetIndex() {
        this.index = 0;
    }
}
