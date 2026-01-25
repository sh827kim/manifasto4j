package ai.manifesto.core;

import ai.manifesto.core.core.Apply;
import ai.manifesto.core.core.Compute;
import ai.manifesto.core.core.Validate;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.expr.literal.Lit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("통합 테스트: Complete Manifesto Workflow")
class IntegrationTest {

    private DomainSchema todoSchema;
    private Snapshot initialSnapshot;

    @BeforeEach
    void setUp() {
        // Todo 애플리케이션 스키마 정의
        FieldSpec titleInputField = FieldSpec.required("title", "string");
        FieldSpec idField = FieldSpec.required("id", "string");
        FieldSpec completedField = new FieldSpec("completed", "boolean", false, false);

        // Root-level 데이터 필드 (선택사항)
        FieldSpec todosField = new FieldSpec("todos", "array", false, false);
        FieldSpec filterField = new FieldSpec("filter", "string", false, false);

        ActionSpec addTodoAction = new ActionSpec.Builder("addTodo")
            .addInputField("title", titleInputField)
            .flow(FlowNode.Halt.of(null))
            .build();

        ActionSpec updateTodoAction = new ActionSpec.Builder("updateTodo")
            .addInputField("id", idField)
            .addInputField("completed", completedField)
            .flow(FlowNode.Halt.of(null))
            .build();

        ComputedFieldDef totalCount = ComputedFieldDef.simple("totalCount", new Lit(0));
        ComputedFieldDef completedCount = ComputedFieldDef.simple("completedCount", new Lit(0));

        todoSchema = buildSchemaWithHash(
            "urn:todo-app",
            "1.0.0",
            new ActionSpec[] { addTodoAction, updateTodoAction },
            new FieldSpec[] { todosField, filterField },
            new ComputedFieldDef[] { totalCount, completedCount }
        );

        // 초기 Snapshot
        Map<String, Object> data = new HashMap<>();
        data.put("todos", new ArrayList<>());
        data.put("filter", "all");

        Map<String, Object> computed = new HashMap<>();
        computed.put("totalCount", 0);
        computed.put("completedCount", 0);

        initialSnapshot = Snapshot.builder()
            .data(data)
            .computed(computed)
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", todoSchema.getHash()))
            .build();
    }

    private DomainSchema buildSchemaWithHash(
        String id,
        String version,
        ActionSpec[] actions,
        FieldSpec[] dataFields,
        ComputedFieldDef[] computedFields
    ) {
        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(id, version);
        for (ActionSpec action : actions) {
            tempBuilder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            tempBuilder.addDataField(field);
        }
        for (ComputedFieldDef computed : computedFields) {
            tempBuilder.addComputedField(computed);
        }
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        DomainSchema.Builder builder = new DomainSchema.Builder(id, version)
            .hash(hash);
        for (ActionSpec action : actions) {
            builder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            builder.addDataField(field);
        }
        for (ComputedFieldDef computed : computedFields) {
            builder.addComputedField(computed);
        }
        return builder.build();
    }

    @Test
    @DisplayName("Snapshot 검증")
    void testValidateSnapshot() {
        assertValid(todoSchema, initialSnapshot);
    }

    @Test
    @DisplayName("Patch 적용 및 버전 증가")
    void testApplyPatch() {
        Patch patch = Patch.set("data.filter", "active");
        Result<Snapshot, ErrorValue> result = Apply.apply(initialSnapshot, patch);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals("active", updated.getData().get("filter"));
        assertEquals(1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("여러 Patch 순차 적용")
    void testApplyMultiplePatches() {
        Patch patch1 = Patch.set("data.filter", "active");
        Patch patch2 = Patch.set("data.sortBy", "date");

        Result<Snapshot, ErrorValue> result = Apply.apply(initialSnapshot, patch1, patch2);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals("active", updated.getData().get("filter"));
        assertEquals("date", updated.getData().get("sortBy"));
        assertEquals(2, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("Intent 발행 및 Compute 실행")
    void testComputeIntent() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Buy groceries");
        Intent intent = new Intent("addTodo", input, UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(todoSchema, initialSnapshot, intent, 5);

        assertNotNull(result);
        assertEquals(ComputeStatus.COMPLETE, result.getStatus());
    }

    @Test
    @DisplayName("상태 변경 및 검증")
    void testStateChangeAndValidation() {
        // 1. 초기 상태 검증
        assertValid(todoSchema, initialSnapshot);

        // 2. Patch 적용
        Patch patch = Patch.set("data.filter", "completed");
        Result<Snapshot, ErrorValue> applyResult = Apply.apply(initialSnapshot, patch);
        assertTrue(applyResult.isOk());

        Snapshot updated = applyResult.unwrap();

        // 3. 변경된 상태 검증
        assertValid(todoSchema, updated);
        assertEquals("completed", updated.getData().get("filter"));
        assertEquals(1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("다양한 액션 실행")
    void testMultipleActionsFlow() throws Exception {
        String intentId1 = UUID.randomUUID().toString();
        String intentId2 = UUID.randomUUID().toString();

        // addTodo 액션 실행
        Map<String, Object> addInput = new HashMap<>();
        addInput.put("title", "Learn Java");
        Intent addIntent = new Intent("addTodo", addInput, intentId1);

        ComputeResult addResult = Compute.computeSync(todoSchema, initialSnapshot, addIntent, 5);
        assertEquals(ComputeStatus.COMPLETE, addResult.getStatus());

        Snapshot afterAdd = addResult.getSnapshot();
        assertValid(todoSchema, afterAdd);

        // updateTodo 액션 실행
        Map<String, Object> updateInput = new HashMap<>();
        updateInput.put("id", "todo-1");
        updateInput.put("completed", true);
        Intent updateIntent = new Intent("updateTodo", updateInput, intentId2);

        ComputeResult updateResult = Compute.computeSync(todoSchema, afterAdd, updateIntent, 5);
        assertEquals(ComputeStatus.COMPLETE, updateResult.getStatus());

        Snapshot afterUpdate = updateResult.getSnapshot();
        assertValid(todoSchema, afterUpdate);
    }

    @Test
    @DisplayName("Snapshot 불변성 보장")
    void testSnapshotImmutability() {
        Map<String, Object> newData = new HashMap<>(initialSnapshot.getData());
        newData.put("filter", "changed");

        Snapshot modified = initialSnapshot.withData(newData);

        // 원본은 변경되지 않음
        assertNotEquals("changed", initialSnapshot.getData().get("filter"));
        assertEquals("all", initialSnapshot.getData().get("filter"));

        // 새 Snapshot은 변경됨
        assertEquals("changed", modified.getData().get("filter"));
    }

    @Test
    @DisplayName("에러 처리: 유효하지 않은 액션")
    void testErrorHandlingInvalidAction() throws Exception {
        Intent invalidIntent = new Intent("nonexistent", new HashMap<>(), UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(todoSchema, initialSnapshot, invalidIntent, 5);

        assertEquals(ComputeStatus.ERROR, result.getStatus());
    }

    @Test
    @DisplayName("스키마 버전 관리")
    void testSchemaVersioning() {
        assertEquals("1.0.0", todoSchema.getVersion());
        assertEquals("urn:todo-app", todoSchema.getId());
        assertNotNull(todoSchema.getHash());
    }

    @Test
    @DisplayName("전체 워크플로우")
    void testCompleteWorkflow() throws Exception {
        // Step 1: 초기 상태 검증
        assertValid(todoSchema, initialSnapshot);

        // Step 2: Intent 발행 및 Compute
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Complete integration test");
        Intent intent = new Intent("addTodo", input, UUID.randomUUID().toString());

        ComputeResult computeResult = Compute.computeSync(todoSchema, initialSnapshot, intent, 5);
        assertTrue(computeResult.getStatus() == ComputeStatus.COMPLETE);

        // Step 3: 변경된 Snapshot 검증
        Snapshot resultSnapshot = computeResult.getSnapshot();
        assertValid(todoSchema, resultSnapshot);

        // Step 4: 추가 Patch 적용
        Patch patch = Patch.set("data.filter", "active");
        Result<Snapshot, ErrorValue> applyResult = Apply.apply(resultSnapshot, patch);
        assertTrue(applyResult.isOk());

        // Step 5: 최종 Snapshot 검증
        Snapshot finalSnapshot = applyResult.unwrap();
        assertValid(todoSchema, finalSnapshot);
        assertEquals(1, finalSnapshot.getMeta().getVersion());
    }

    private void assertValid(DomainSchema schema, Snapshot snapshot) {
        Validate.ValidationResult result = Validate.validate(schema, snapshot);
        assertTrue(result.isValid(), result.errors().toString());
    }
}
