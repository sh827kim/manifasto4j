package ai.manifesto.host;

import java.util.Map;

/**
 * EffectHandler - Requirement를 실행하고 Patch를 생성한다.
 */
@FunctionalInterface
public interface EffectHandler {
    EffectResult handle(Map<String, Object> params);
}
