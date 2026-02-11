package ai.manifesto.world.schema;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * KR: ActorRef는 World 도메인 객체를 참조하기 위한 참조 타입입니다.
 * EN: ActorRef is a reference type used to point to a World-domain object.
 */
public final class ActorRef {
    private final String actorId;
    private final ActorKind kind;
    private final String name;
    private final Set<String> roles;

    public ActorRef(String actorId, ActorKind kind) {
        this(actorId, kind, null, Set.of());
    }

    public ActorRef(String actorId, ActorKind kind, String name, Set<String> roles) {
        this.actorId = Objects.requireNonNull(actorId, "actorId is required");
        this.kind = Objects.requireNonNull(kind, "kind is required");
        this.name = name;
        this.roles = Collections.unmodifiableSet(new LinkedHashSet<>(roles != null ? roles : Set.of()));
    }

    public String getActorId() {
        return actorId;
    }

    public ActorKind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActorRef actorRef)) return false;
        return Objects.equals(actorId, actorRef.actorId)
                && kind == actorRef.kind
                && Objects.equals(name, actorRef.name)
                && Objects.equals(roles, actorRef.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actorId, kind, name, roles);
    }
}
