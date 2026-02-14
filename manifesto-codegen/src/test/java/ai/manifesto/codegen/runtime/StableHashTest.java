package ai.manifesto.codegen.runtime;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StableHashTest {

    @Test
    void stableHashProducesDeterministicCanonicalHash() {
        String a = StableHash.stableHash(Map.of("foo", 1, "bar", 2));
        String b = StableHash.stableHash(Map.of("bar", 2, "foo", 1));
        String c = StableHash.stableHash(Map.of("foo", 3, "bar", 2));

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertTrue(a.matches("^[0-9a-f]{64}$"));

        Map<String, Object> ordered = new LinkedHashMap<>();
        ordered.put("z", 1);
        ordered.put("a", 2);
        assertEquals(StableHash.stableHash(ordered), StableHash.stableHash(Map.of("a", 2, "z", 1)));
    }
}
