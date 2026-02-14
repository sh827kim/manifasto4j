package ai.manifesto.intentir;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntentIrKeyDeriverTest {

    @Test
    void strictKeyChangesWhenMetaChanges() {
        IntentIrKeyDeriver deriver = new IntentIrKeyDeriver();
        IntentIrDocument left = new IntentIrDocument(
            "1.0.0",
            "todo",
            "add",
            Map.of("title", "A"),
            Map.of("requestId", "req-1", "traceId", "t-1")
        );
        IntentIrDocument right = new IntentIrDocument(
            "1.0.0",
            "todo",
            "add",
            Map.of("title", "A"),
            Map.of("requestId", "req-2", "traceId", "t-2")
        );

        assertNotEquals(deriver.deriveStrictKey(left), deriver.deriveStrictKey(right));
    }

    @Test
    void semanticKeyIgnoresVolatileMetaFields() {
        IntentIrKeyDeriver deriver = new IntentIrKeyDeriver();
        IntentIrDocument left = new IntentIrDocument(
            "1.0.0",
            "todo",
            "add",
            Map.of("title", "A"),
            Map.of("requestId", "req-1", "traceId", "t-1", "channel", "chat")
        );
        IntentIrDocument right = new IntentIrDocument(
            "1.0.0",
            "todo",
            "add",
            Map.of("title", "A"),
            Map.of("requestId", "req-2", "traceId", "t-2", "channel", "chat")
        );

        assertEquals(deriver.deriveSemanticKey(left), deriver.deriveSemanticKey(right));
    }

    @Test
    void simKeyDistanceIsSmallForNearDocuments() {
        IntentIrKeyDeriver deriver = new IntentIrKeyDeriver();
        IntentIrDocument left = new IntentIrDocument(
            "1.0.0",
            "todo",
            "add",
            Map.of("text", "buy milk today"),
            Map.of("channel", "chat")
        );
        IntentIrDocument right = new IntentIrDocument(
            "1.0.0",
            "todo",
            "add",
            Map.of("text", "buy milk tomorrow"),
            Map.of("channel", "chat")
        );

        String leftKey = deriver.deriveSimKey(left);
        String rightKey = deriver.deriveSimKey(right);
        int distance = deriver.simDistance(leftKey, rightKey);
        assertTrue(distance >= 0 && distance <= 64);
        assertTrue(deriver.isNearDuplicate(leftKey, rightKey, 20));
    }
}
