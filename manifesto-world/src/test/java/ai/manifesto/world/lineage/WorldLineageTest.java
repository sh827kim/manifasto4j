package ai.manifesto.world.lineage;

import ai.manifesto.world.schema.DecisionId;
import ai.manifesto.world.schema.ProposalId;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.schema.WorldId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldLineageTest {
    private WorldLineage lineage;

    @BeforeEach
    void setUp() {
        lineage = new WorldLineage();
    }

    @Test
    void supportsGenesisAndBranching() {
        World genesis = world("world-1", null);
        lineage.setGenesis(genesis);

        World childA = world("world-2", ProposalId.of("prop-1"));
        World childB = world("world-3", ProposalId.of("prop-2"));

        lineage.addWorldWithEdge(childA, genesis.getWorldId(), ProposalId.of("prop-1"), DecisionId.of("dec-1"), 100L);
        lineage.addWorldWithEdge(childB, genesis.getWorldId(), ProposalId.of("prop-2"), DecisionId.of("dec-2"), 110L);

        assertTrue(lineage.hasGenesis());
        assertEquals(3, lineage.getWorldCount());
        assertEquals(2, lineage.getEdgeCount());
        assertEquals(2, lineage.getChildren(genesis.getWorldId()).size());
    }

    @Test
    void rejectsSecondGenesis() {
        lineage.setGenesis(world("world-1", null));
        assertThrows(IllegalStateException.class, () -> lineage.setGenesis(world("world-2", null)));
    }

    @Test
    void returnsAncestors() {
        World genesis = world("world-1", null);
        lineage.setGenesis(genesis);

        World child = world("world-2", ProposalId.of("prop-1"));
        lineage.addWorldWithEdge(child, genesis.getWorldId(), ProposalId.of("prop-1"), DecisionId.of("dec-1"), 100L);

        World grandchild = world("world-3", ProposalId.of("prop-2"));
        lineage.addWorldWithEdge(grandchild, child.getWorldId(), ProposalId.of("prop-2"), DecisionId.of("dec-2"), 120L);

        assertEquals(2, lineage.getAncestors(grandchild.getWorldId()).size());
        assertEquals(2, lineage.getDepth(grandchild.getWorldId()));
        assertNotNull(lineage.getParent(grandchild.getWorldId()));
    }

    private static World world(String worldId, ProposalId createdBy) {
        return new World(WorldId.of(worldId), "schema", "snapshot-" + worldId, 1L, createdBy, null);
    }
}
