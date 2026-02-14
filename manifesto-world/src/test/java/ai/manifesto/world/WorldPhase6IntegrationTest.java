package ai.manifesto.world;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.world.events.InMemoryWorldEventJournal;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
import ai.manifesto.world.schema.IntentBody;
import ai.manifesto.world.schema.IntentInstance;
import ai.manifesto.world.schema.IntentMeta;
import ai.manifesto.world.schema.IntentOrigin;
import ai.manifesto.world.schema.IntentSource;
import ai.manifesto.world.types.HostExecutionResult;
import ai.manifesto.world.types.IntentKeys;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorldPhase6IntegrationTest {

    @Test
    void eventJournalAndProposalStatusQueryWorkTogether() {
        InMemoryWorldEventJournal journal = new InMemoryWorldEventJournal();
        ManifestoWorld world = new ManifestoWorld(
            "schema-hash",
            (executionKey, baseSnapshot, intent, options) -> HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1))),
            null,
            journal
        );

        Snapshot genesisSnapshot = Snapshot.builder()
            .data(Map.of("count", 0))
            .computed(Map.of())
            .system(SystemState.initial())
            .input(Map.of())
            .meta(Snapshot.SnapshotMeta.create(1, 1000L, "seed", "schema-hash"))
            .build();
        var genesis = world.createGenesis(genesisSnapshot);

        ActorRef actor = new ActorRef("human-phase6", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());

        IntentBody body = new IntentBody("increment", Map.of("n", 1), null);
        IntentInstance intent = new IntentInstance(
            body,
            "intent-phase6",
            IntentKeys.computeIntentKey("schema-hash", body),
            new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", "event-phase6"), actor))
        );

        ProposalResult result = world.submitProposal(actor.getActorId(), intent, genesis.getWorldId(), null);
        assertNotNull(result.getProposal());

        assertTrue(journal.queryByType("proposal:submitted").size() >= 1);
        assertTrue(journal.queryByType("proposal:decided").size() >= 1);
        assertTrue(journal.queryByType("execution:completed").size() >= 1);

        assertEquals(0, world.getStore().listProposalsByStatus(ai.manifesto.world.schema.ProposalStatus.EVALUATING).size());
        assertEquals(1, world.getStore().listProposalsByStatus(ai.manifesto.world.schema.ProposalStatus.COMPLETED).size());
    }
}
