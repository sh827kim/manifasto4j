package ai.manifesto.sdk;

/**
 * KR: 액션 단계 업데이트 이벤트입니다.
 * EN: Action phase update event.
 */
public record ActionUpdate(
    ActionPhase phase,
    String message,
    long timestamp
) {
}
