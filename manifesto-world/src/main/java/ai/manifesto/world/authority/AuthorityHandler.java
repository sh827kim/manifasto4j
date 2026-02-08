package ai.manifesto.world.authority;

import ai.manifesto.world.schema.ActorAuthorityBinding;
import ai.manifesto.world.schema.Proposal;

public interface AuthorityHandler {
    AuthorityResponse evaluate(Proposal proposal, ActorAuthorityBinding binding);
}
