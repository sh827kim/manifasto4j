package ai.manifesto.world.ingress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngressContextTest {

    @Test
    void tracksEpochAndStaleness() {
        IngressContext context = new IngressContext();
        assertEquals(0L, context.epoch());
        assertFalse(context.isStale(0L));

        context.incrementEpoch();
        assertEquals(1L, context.epoch());
        assertTrue(context.isStale(0L));
        assertFalse(context.isStale(1L));
    }

    @Test
    void rejectsNegativeInitialEpoch() {
        assertThrows(IllegalArgumentException.class, () -> new IngressContext(-1L));
    }
}
