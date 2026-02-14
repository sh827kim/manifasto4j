package ai.manifesto.app;

import java.util.Map;

/**
 * KR: `system.*` 액션 실행을 위한 파사드 계약입니다.
 * EN: Facade contract for invoking `system.*` actions.
 */
public interface SystemFacade {
    ActionHandle act(String systemActionType, Map<String, Object> input) throws Exception;
}
