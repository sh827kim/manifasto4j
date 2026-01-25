package ai.manifesto.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Patch 테스트")
class PatchTest {

    @Test
    @DisplayName("SET Patch 생성")
    void testSetPatch() {
        Patch patch = Patch.set("data.count", 42);
        assertNotNull(patch);
        assertEquals("data.count", patch.getPath());
        assertInstanceOf(Patch.Set.class, patch);
    }

    @Test
    @DisplayName("UNSET Patch 생성")
    void testUnsetPatch() {
        Patch patch = Patch.unset("data.user");
        assertNotNull(patch);
        assertEquals("data.user", patch.getPath());
        assertInstanceOf(Patch.Unset.class, patch);
    }

    @Test
    @DisplayName("MERGE Patch 생성")
    void testMergePatch() {
        Map<String, Object> values = new HashMap<>();
        values.put("updated", true);
        values.put("timestamp", System.currentTimeMillis());

        Patch patch = Patch.merge("data.metadata", values);
        assertNotNull(patch);
        assertEquals("data.metadata", patch.getPath());
        assertInstanceOf(Patch.Merge.class, patch);
    }

    @Test
    @DisplayName("여러 Patch 조합")
    void testMultiplePatches() {
        Patch patch1 = Patch.set("data.count", 1);
        Patch patch2 = Patch.set("data.name", "test");
        Patch patch3 = Patch.unset("data.old");

        assertNotNull(patch1);
        assertNotNull(patch2);
        assertNotNull(patch3);

        assertEquals("data.count", patch1.getPath());
        assertEquals("data.name", patch2.getPath());
        assertEquals("data.old", patch3.getPath());
    }

    @Test
    @DisplayName("SET Patch with null value")
    void testSetPatchWithNull() {
        Patch patch = Patch.set("data.value", null);
        assertNotNull(patch);
        assertEquals("data.value", patch.getPath());
    }

    @Test
    @DisplayName("경로 유효성 검증")
    void testPathValidation() {
        // 정상 경로
        assertDoesNotThrow(() -> Patch.set("data.field", "value"));
        assertDoesNotThrow(() -> Patch.set("computed.result", 42));
        assertDoesNotThrow(() -> Patch.set("system.status", "idle"));
    }

    @Test
    @DisplayName("MERGE Patch with empty map")
    void testMergePatchWithEmptyMap() {
        Map<String, Object> values = new HashMap<>();
        Patch patch = Patch.merge("data.config", values);
        assertNotNull(patch);
        assertEquals("data.config", patch.getPath());
    }
}
