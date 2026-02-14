package ai.manifesto.host;

import java.util.Map;

/**
 * KR: execution context를 함께 받는 확장 effect handler 계약입니다.
 * EN: Extended effect handler contract that receives execution context.
 */
@FunctionalInterface
public interface ContextAwareEffectHandler extends EffectHandler {
    EffectResult handle(Map<String, Object> params, EffectExecutionContext context);

    @Override
    default EffectResult handle(Map<String, Object> params) {
        return handle(params, EffectExecutionContext.unknown());
    }
}
