package ai.manifesto.core.trace;

/**
 * KR: TraceContext는 실행 시점의 컨텍스트 값(시간, 환경, 상태 참조 등)을 전달하는 타입입니다.
 * EN: TraceContext is a context type carrying runtime values such as time, environment, and state references.
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
