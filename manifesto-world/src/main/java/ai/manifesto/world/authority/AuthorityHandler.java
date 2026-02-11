package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.Proposal;

/**
 * KR: AuthorityHandler는 특정 도메인 이벤트/요청을 처리하는 핸들러 타입입니다.
 * EN: AuthorityHandler is a handler type that processes specific domain events or requests.
 */
public interface AuthorityHandler {
    AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding);
}
