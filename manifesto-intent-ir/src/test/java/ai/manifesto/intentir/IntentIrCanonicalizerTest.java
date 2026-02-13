package ai.manifesto.intentir;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntentIrCanonicalizerTest {

    @Test
    void canonicalJsonIsStableForEquivalentMaps() {
        IntentIrCanonicalizer canonicalizer = new IntentIrCanonicalizer();

        Map<String, Object> inputA = new LinkedHashMap<>();
        inputA.put("b", 2);
        inputA.put("a", 1);
        inputA.put("nested", new LinkedHashMap<>(Map.of("z", true, "x", false)));
        inputA.put("list", List.of(
            new LinkedHashMap<>(Map.of("k2", "v2", "k1", "v1"))
        ));

        Map<String, Object> inputB = new LinkedHashMap<>();
        inputB.put("list", List.of(
            new LinkedHashMap<>(Map.of("k1", "v1", "k2", "v2"))
        ));
        inputB.put("nested", new LinkedHashMap<>(Map.of("x", false, "z", true)));
        inputB.put("a", 1);
        inputB.put("b", 2);

        IntentIrDocument docA = new IntentIrDocument(
            "1.0.0",
            "todo",
            "create",
            inputA,
            Map.of("requestId", "r-1")
        );
        IntentIrDocument docB = new IntentIrDocument(
            "1.0.0",
            "todo",
            "create",
            inputB,
            Map.of("requestId", "r-1")
        );

        String canonicalA = canonicalizer.toCanonicalJson(docA);
        String canonicalB = canonicalizer.toCanonicalJson(docB);

        assertEquals(canonicalA, canonicalB);
        assertTrue(canonicalA.contains("\"schemaVersion\":\"1.0.0\""));
    }

    @Test
    void hashingIsStableAndDetectsChanges() {
        IntentIrHashing hashing = new IntentIrHashing();

        IntentIrDocument base = new IntentIrDocument(
            "1.0.0",
            "todo",
            "create",
            Map.of("title", "A"),
            Map.of("requestId", "r-1")
        );
        IntentIrDocument same = new IntentIrDocument(
            "1.0.0",
            "todo",
            "create",
            Map.of("title", "A"),
            Map.of("requestId", "r-1")
        );
        IntentIrDocument changed = new IntentIrDocument(
            "1.0.0",
            "todo",
            "create",
            Map.of("title", "B"),
            Map.of("requestId", "r-1")
        );

        String baseHash = hashing.hash(base);
        String sameHash = hashing.hash(same);
        String changedHash = hashing.hash(changed);

        assertEquals(baseHash, sameHash);
        assertNotEquals(baseHash, changedHash);
    }

    @Test
    void normalizerRejectsBlankRequiredFields() {
        DefaultIntentIrNormalizer normalizer = new DefaultIntentIrNormalizer();
        IntentIrDocument invalid = new IntentIrDocument(
            " ",
            "todo",
            "create",
            Map.of(),
            Map.of()
        );

        assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(invalid));
    }
}
