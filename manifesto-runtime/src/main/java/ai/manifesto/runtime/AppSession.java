package ai.manifesto.runtime;

import java.util.Map;

/**
 * KR: actor/context가 고정된 액션 실행 세션 계약입니다.
 * EN: Action execution session contract with fixed actor/context.
 */
public interface AppSession {
    String actorId();

    Map<String, Object> context();

    ActionHandle act(String actionType, Map<String, Object> input) throws Exception;

    AppSession withContext(String key, Object value);
}
