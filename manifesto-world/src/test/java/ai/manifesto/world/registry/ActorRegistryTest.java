package ai.manifesto.world.registry;

import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
import ai.manifesto.world.schema.AuthorityKind;
import ai.manifesto.world.schema.AuthorityRef;
import ai.manifesto.world.schema.HitlPolicy;
import ai.manifesto.world.schema.TimeoutAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActorRegistryTest {
    private ActorRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ActorRegistry();
    }

    @Test
    void registersAndUpdatesBinding() {
        ActorRef alice = new ActorRef("alice", ActorKind.HUMAN);
        registry.register(
                alice,
                new AuthorityRef("auto", AuthorityKind.AUTO),
                new AutoApprovePolicy("trusted")
        );

        assertTrue(registry.isRegistered("alice"));
        assertEquals(1, registry.size());

        registry.updateBinding(
                "alice",
                new AuthorityRef("human-review", AuthorityKind.HUMAN),
                new HitlPolicy(alice, 60000L, TimeoutAction.REJECT)
        );

        assertEquals(AuthorityKind.HUMAN, registry.getBindingOrThrow("alice").getAuthority().getKind());
    }

    @Test
    void rejectsDuplicateRegistration() {
        ActorRef alice = new ActorRef("alice", ActorKind.HUMAN);
        registry.register(
                alice,
                new AuthorityRef("auto", AuthorityKind.AUTO),
                new AutoApprovePolicy()
        );

        assertThrows(IllegalArgumentException.class, () -> registry.register(
                alice,
                new AuthorityRef("another", AuthorityKind.AUTO),
                new AutoApprovePolicy()
        ));
    }
}
