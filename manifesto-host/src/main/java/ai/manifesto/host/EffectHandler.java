package ai.manifesto.host;

import java.util.Map;

/**
 * KR: Host가 Core Requirement를 실제 부수효과로 실행할 때 사용하는 핸들러 계약입니다.
 * EN: Contract for executing a Core Requirement as a host-side effect.
 */
@FunctionalInterface
public interface EffectHandler {
    EffectResult handle(Map<String, Object> params);
}
