package ai.manifesto.sdk;

import java.util.Map;

/**
 * KR: SDK `system.*` 액션 실행 파사드입니다.
 * EN: SDK facade for invoking `system.*` actions.
 */
public interface SystemFacade {
    ActionHandle act(String systemActionType, Map<String, Object> input) throws Exception;
}
