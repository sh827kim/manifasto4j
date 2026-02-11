package ai.manifesto.host;

import java.util.Map;

/**
 * KR: EffectHandler는 특정 도메인 이벤트/요청을 처리하는 핸들러 타입입니다.
 * EN: EffectHandler is a handler type that processes specific domain events or requests.
 */
@FunctionalInterface
public interface EffectHandler {
    EffectResult handle(Map<String, Object> params);
}
