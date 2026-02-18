package ai.manifesto.runtime;

import java.util.Map;

/**
 * KR: SessionOptions는 session 생성 시 actor/context를 전달하는 옵션 계약입니다.
 * EN: SessionOptions carries actor and context options when creating sessions.
 */
public record SessionOptions(
    String actorId,
    Map<String, Object> context
) {
}
