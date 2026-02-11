package ai.manifesto.world.schema;

import java.util.Objects;

/**
 * KR: ActorAuthorityBinding는 World 스키마 계층에서 actor authority binding 역할을 수행하는 구현 타입입니다.
 * EN: ActorAuthorityBinding is an implementation type performing actor authority binding roles in the World schema layer.
 */
public final class ActorAuthorityBinding {
    private final ActorRef actor;
    private final AuthorityRef authority;
    private final AuthorityPolicy policy;

    public ActorAuthorityBinding(ActorRef actor, AuthorityRef authority, AuthorityPolicy policy) {
        this.actor = Objects.requireNonNull(actor, "actor is required");
        this.authority = Objects.requireNonNull(authority, "authority is required");
        this.policy = Objects.requireNonNull(policy, "policy is required");
    }

    public ActorRef getActor() { return actor; }
    public AuthorityRef getAuthority() { return authority; }
    public AuthorityPolicy getPolicy() { return policy; }
}
