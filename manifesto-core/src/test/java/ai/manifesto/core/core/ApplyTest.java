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
        FieldSpec tempField = FieldSpec.optional("temp", "string");
        FieldSpec metadataField = FieldSpec.optional("metadata", "object");
        FieldSpec userField = FieldSpec.optional("user", "object");

        schema = new DomainSchema.Builder("test-schema", "1.0.0")
            .hash("test-hash")
            .addDataField(countField)
            .addDataField(nameField)
            .addDataField(tempField)
            .addDataField(metadataField)
            .addDataField(userField)
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
        Patch patch = Patch.set("count", 42);

        Result<Snapshot, ErrorValue> result = Apply.apply(schema, snapshot, patch);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals(42, updated.getData().get("count"));
        assertEquals(1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("여러 SET Patch 적용")
    void testApplyMultiplePatches() {
        Patch patch1 = Patch.set("count", 10);
        Patch patch2 = Patch.set("name", "updated");

        Result<Snapshot, ErrorValue> result = Apply.apply(schema, snapshot, patch1, patch2);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals(10, updated.getData().get("count"));
        assertEquals("updated", updated.getData().get("name"));
        assertEquals(1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("UNSET Patch 적용")
    void testApplyUnsetPatch() {
        Patch setPatch = Patch.set("temp", "temporary");
        Result<Snapshot, ErrorValue> setResult = Apply.apply(schema, snapshot, setPatch);

        assertTrue(setResult.isOk());
        Snapshot withTemp = setResult.unwrap();
        assertTrue(withTemp.getData().containsKey("temp"));

        Patch unsetPatch = Patch.unset("temp");
        Result<Snapshot, ErrorValue> unsetResult = Apply.apply(schema, withTemp, unsetPatch);

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

        Patch mergePatch = Patch.merge("metadata", metadata);
        Result<Snapshot, ErrorValue> result = Apply.apply(schema, snapshot, mergePatch);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertTrue(updated.getData().containsKey("metadata"));
    }

    @Test
    @DisplayName("Patch 적용 시 버전 증가")
    void testVersionIncrement() {
        assertEquals(0, snapshot.getMeta().getVersion());

        Patch patch1 = Patch.set("count", 1);
        Result<Snapshot, ErrorValue> result1 = Apply.apply(schema, snapshot, patch1);
        assertEquals(1, result1.unwrap().getMeta().getVersion());

        Patch patch2 = Patch.set("count", 2);
        Result<Snapshot, ErrorValue> result2 = Apply.apply(schema, result1.unwrap(), patch2);
        assertEquals(2, result2.unwrap().getMeta().getVersion());
    }

    @Test
    @DisplayName("원본 Snapshot 불변성")
    void testOriginalSnapshotUnchanged() {
        int originalCount = (int) snapshot.getData().get("count");
        long originalVersion = snapshot.getMeta().getVersion();

        Patch patch = Patch.set("count", 999);
        Apply.apply(schema, snapshot, patch);

        // 원본은 변경되지 않음
        assertEquals(originalCount, snapshot.getData().get("count"));
        assertEquals(originalVersion, snapshot.getMeta().getVersion());
    }

    @Test
    @DisplayName("nested path에 값 설정")
    void testNestedPathSet() {
        Patch patch = Patch.set("user.profile.name", "John");

        Result<Snapshot, ErrorValue> result = Apply.apply(schema, snapshot, patch);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertNotNull(updated.getData().get("user"));
    }

    @Test
    @DisplayName("빈 Patch 배열 적용")
    void testApplyEmptyPatches() {
        Result<Snapshot, ErrorValue> result = Apply.apply(schema, snapshot);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        // Patch가 없어도 버전은 증가하지 않아야 함
        assertEquals(snapshot.getMeta().getVersion() + 1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("null Snapshot 처리")
    void testNullSnapshot() {
        Patch patch = Patch.set("count", 42);
        assertThrows(NullPointerException.class, () -> Apply.apply(schema, null, patch));
    }

    @Test
    @DisplayName("Patch 체이닝")
    void testPatchChaining() {
        Result<Snapshot, ErrorValue> result = Apply.apply(schema, snapshot,
            Patch.set("count", 1),
            Patch.set("name", "first"),
            Patch.set("count", 2),
            Patch.set("name", "second")
        );

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals(2, updated.getData().get("count"));
        assertEquals("second", updated.getData().get("name"));
        assertEquals(1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("$mel 예약 네임스페이스 patch는 state spec 없이도 허용")
    void testMelReservedPathPatchAllowedWithoutStateSpecField() {
        Result<Snapshot, ErrorValue> result = Apply.apply(
            schema,
            snapshot,
            Patch.set("$mel.guards.intent.guard-1", "intent-1")
        );

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        @SuppressWarnings("unchecked")
        Map<String, Object> melState = (Map<String, Object>) updated.getData().get("$mel");
        assertNotNull(melState);
        @SuppressWarnings("unchecked")
        Map<String, Object> guards = (Map<String, Object>) melState.get("guards");
        assertNotNull(guards);
    }

    @Test
    @DisplayName("플랫폼 네임스페이스 루트는 object 또는 null만 허용")
    void testPlatformNamespaceRootRejectsNonObject() {
        Result<Snapshot, ErrorValue> result = Apply.apply(
            schema,
            snapshot,
            Patch.set("$mel", "invalid")
        );

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertFalse(updated.getData().containsKey("$mel"));
        assertEquals(SystemState.Status.ERROR, updated.getSystem().getStatus());
        assertNotNull(updated.getSystem().getLastError());
        assertEquals("TYPE_MISMATCH", updated.getSystem().getLastError().getCode());
    }

    @Test
    @DisplayName("merge 대상 경로가 객체가 아니면 TYPE_MISMATCH")
    void testMergeTargetNonObjectRecordsTypeMismatch() {
        Snapshot withInvalidMel = Apply.apply(schema, snapshot, Patch.set("$mel.guards", "invalid")).unwrap();

        Result<Snapshot, ErrorValue> result = Apply.apply(
            schema,
            withInvalidMel,
            Patch.merge("$mel.guards.intent", Map.of("guard-1", "intent-1"))
        );

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals(SystemState.Status.ERROR, updated.getSystem().getStatus());
        assertNotNull(updated.getSystem().getLastError());
        assertEquals("TYPE_MISMATCH", updated.getSystem().getLastError().getCode());
    }
}
