package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Apply Patch 적용 테스트")
class ApplyTest {

    private DomainSchema schema;
    private Snapshot snapshot;

    @BeforeEach
    void setUp() {
        FieldSpec countField = FieldSpec.required("count", "integer");
        FieldSpec nameField = FieldSpec.required("name", "string");

        schema = new DomainSchema.Builder("test-schema", "1.0.0")
            .hash("test-hash")
            .addDataField(countField)
            .addDataField(nameField)
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("count", 0);
        data.put("name", "initial");

        snapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();
    }

    @Test
    @DisplayName("단일 SET Patch 적용")
    void testApplySingleSetPatch() {
        Patch patch = Patch.set("data.count", 42);

        Result<Snapshot, ErrorValue> result = Apply.apply(snapshot, patch);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals(42, updated.getData().get("count"));
        assertEquals(1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("여러 SET Patch 적용")
    void testApplyMultiplePatches() {
        Patch patch1 = Patch.set("data.count", 10);
        Patch patch2 = Patch.set("data.name", "updated");

        Result<Snapshot, ErrorValue> result = Apply.apply(snapshot, patch1, patch2);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals(10, updated.getData().get("count"));
        assertEquals("updated", updated.getData().get("name"));
        assertEquals(2, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("UNSET Patch 적용")
    void testApplyUnsetPatch() {
        Patch setPatch = Patch.set("data.temp", "temporary");
        Result<Snapshot, ErrorValue> setResult = Apply.apply(snapshot, setPatch);

        assertTrue(setResult.isOk());
        Snapshot withTemp = setResult.unwrap();
        assertTrue(withTemp.getData().containsKey("temp"));

        Patch unsetPatch = Patch.unset("data.temp");
        Result<Snapshot, ErrorValue> unsetResult = Apply.apply(withTemp, unsetPatch);

        assertTrue(unsetResult.isOk());
        Snapshot removed = unsetResult.unwrap();
        assertFalse(removed.getData().containsKey("temp"));
    }

    @Test
    @DisplayName("MERGE Patch 적용")
    void testApplyMergePatch() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("created", true);
        metadata.put("timestamp", 12345);

        Patch mergePatch = Patch.merge("data.metadata", metadata);
        Result<Snapshot, ErrorValue> result = Apply.apply(snapshot, mergePatch);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertTrue(updated.getData().containsKey("metadata"));
    }

    @Test
    @DisplayName("Patch 적용 시 버전 증가")
    void testVersionIncrement() {
        assertEquals(0, snapshot.getMeta().getVersion());

        Patch patch1 = Patch.set("data.count", 1);
        Result<Snapshot, ErrorValue> result1 = Apply.apply(snapshot, patch1);
        assertEquals(1, result1.unwrap().getMeta().getVersion());

        Patch patch2 = Patch.set("data.count", 2);
        Result<Snapshot, ErrorValue> result2 = Apply.apply(result1.unwrap(), patch2);
        assertEquals(2, result2.unwrap().getMeta().getVersion());
    }

    @Test
    @DisplayName("원본 Snapshot 불변성")
    void testOriginalSnapshotUnchanged() {
        int originalCount = (int) snapshot.getData().get("count");
        long originalVersion = snapshot.getMeta().getVersion();

        Patch patch = Patch.set("data.count", 999);
        Apply.apply(snapshot, patch);

        // 원본은 변경되지 않음
        assertEquals(originalCount, snapshot.getData().get("count"));
        assertEquals(originalVersion, snapshot.getMeta().getVersion());
    }

    @Test
    @DisplayName("nested path에 값 설정")
    void testNestedPathSet() {
        Patch patch = Patch.set("data.user.profile.name", "John");

        Result<Snapshot, ErrorValue> result = Apply.apply(snapshot, patch);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertNotNull(updated.getData().get("user"));
    }

    @Test
    @DisplayName("빈 Patch 배열 적용")
    void testApplyEmptyPatches() {
        Result<Snapshot, ErrorValue> result = Apply.apply(snapshot);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        // Patch가 없어도 버전은 증가하지 않아야 함
        assertEquals(snapshot.getMeta().getVersion(), updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("null Snapshot 처리")
    void testNullSnapshot() {
        Patch patch = Patch.set("data.count", 42);
        Result<Snapshot, ErrorValue> result = Apply.apply(null, patch);
        assertTrue(result.isErr());
    }

    @Test
    @DisplayName("Patch 체이닝")
    void testPatchChaining() {
        Result<Snapshot, ErrorValue> result = Apply.apply(snapshot,
            Patch.set("data.count", 1),
            Patch.set("data.name", "first"),
            Patch.set("data.count", 2),
            Patch.set("data.name", "second")
        );

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals(2, updated.getData().get("count"));
        assertEquals("second", updated.getData().get("name"));
        assertEquals(4, updated.getMeta().getVersion());
    }
}
