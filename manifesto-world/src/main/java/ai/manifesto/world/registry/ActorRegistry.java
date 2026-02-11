package ai.manifesto.world.registry;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AuthorityPolicy;
import ai.manifesto.world.schema.AuthorityRef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: ActorRegistry는 키-값 등록 정보를 관리하는 레지스트리 컴포넌트입니다.
 * EN: ActorRegistry is a registry component that manages keyed registration metadata.
 */
public final class ActorRegistry {
    private final Map<String, ActorRef> actors = new LinkedHashMap<>();
    private final Map<String, ActorAuthorityBinding> bindings = new LinkedHashMap<>();

    public void register(ActorRef actor, AuthorityRef authority, AuthorityPolicy policy) {
        Objects.requireNonNull(actor, "actor is required");
        Objects.requireNonNull(authority, "authority is required");
        Objects.requireNonNull(policy, "policy is required");

        if (actors.containsKey(actor.getActorId())) {
            throw new IllegalArgumentException("Actor already registered: " + actor.getActorId());
        }

        actors.put(actor.getActorId(), actor);
        bindings.put(actor.getActorId(), new ActorAuthorityBinding(actor, authority, policy));
    }

    public boolean unregister(String actorId) {
        if (!actors.containsKey(actorId)) {
            return false;
        }
        actors.remove(actorId);
        bindings.remove(actorId);
        return true;
    }

    public ActorRef getActorOrThrow(String actorId) {
        ActorRef actor = actors.get(actorId);
        if (actor == null) {
            throw new IllegalArgumentException("Actor not registered: " + actorId);
        }
        return actor;
    }

    public ActorRef getActor(String actorId) {
        return actors.get(actorId);
    }

    public ActorAuthorityBinding getBindingOrThrow(String actorId) {
        ActorAuthorityBinding binding = bindings.get(actorId);
        if (binding == null) {
            if (!actors.containsKey(actorId)) {
                throw new IllegalArgumentException("Actor not registered: " + actorId);
            }
            throw new IllegalStateException("Actor is unbound: " + actorId);
        }
        return binding;
    }

    public ActorAuthorityBinding getBinding(String actorId) {
        return bindings.get(actorId);
    }

    public void updateBinding(String actorId, AuthorityRef authority, AuthorityPolicy policy) {
        Objects.requireNonNull(authority, "authority is required");
        Objects.requireNonNull(policy, "policy is required");

        ActorRef actor = actors.get(actorId);
        if (actor == null) {
            throw new IllegalArgumentException("Actor not registered: " + actorId);
        }

        bindings.put(actorId, new ActorAuthorityBinding(actor, authority, policy));
    }

    public boolean isRegistered(String actorId) {
        return actors.containsKey(actorId);
    }

    public boolean isBound(String actorId) {
        return bindings.containsKey(actorId);
    }

    public List<ActorRef> listActors() {
        return new ArrayList<>(actors.values());
    }

    public List<ActorAuthorityBinding> listBindings() {
        return new ArrayList<>(bindings.values());
    }

    public List<ActorRef> getActorsByAuthority(String authorityId) {
        List<ActorRef> result = new ArrayList<>();
        for (ActorAuthorityBinding binding : bindings.values()) {
            if (binding.getAuthority().getAuthorityId().equals(authorityId)) {
                result.add(binding.getActor());
            }
        }
        return result;
    }

    public List<ActorRef> getActorsByKind(ActorKind kind) {
        List<ActorRef> result = new ArrayList<>();
        for (ActorRef actor : actors.values()) {
            if (actor.getKind() == kind) {
                result.add(actor);
            }
        }
        return result;
    }

    public int size() {
        return actors.size();
    }

    public void clear() {
        actors.clear();
        bindings.clear();
    }
}
