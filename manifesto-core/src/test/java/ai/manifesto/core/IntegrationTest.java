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
import java.util.List;
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
        FieldSpec sortByField = new FieldSpec("sortBy", "string", false, false);

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
            new FieldSpec[] { todosField, filterField, sortByField },
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
        Result<Snapshot, ErrorValue> result = Apply.apply(todoSchema, initialSnapshot, patch);

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

        Result<Snapshot, ErrorValue> result = Apply.apply(todoSchema, initialSnapshot, patch1, patch2);

        assertTrue(result.isOk());
        Snapshot updated = result.unwrap();
        assertEquals("active", updated.getData().get("filter"));
        assertEquals("date", updated.getData().get("sortBy"));
        assertEquals(1, updated.getMeta().getVersion());
    }

    @Test
    @DisplayName("Intent 발행 및 Compute 실행")
    void testComputeIntent() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Buy groceries");
        Intent intent = new Intent("addTodo", input, UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(todoSchema, initialSnapshot, intent, 5);

        assertNotNull(result);
        assertEquals(ComputeStatus.HALTED, result.getStatus());
    }

    @Test
    @DisplayName("상태 변경 및 검증")
    void testStateChangeAndValidation() {
        // 1. 초기 상태 검증
        assertValid(todoSchema, initialSnapshot);

        // 2. Patch 적용
        Patch patch = Patch.set("data.filter", "completed");
        Result<Snapshot, ErrorValue> applyResult = Apply.apply(todoSchema, initialSnapshot, patch);
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
        assertEquals(ComputeStatus.HALTED, addResult.getStatus());

        Snapshot afterAdd = addResult.getSnapshot();
        assertValid(todoSchema, afterAdd);

        // updateTodo 액션 실행
        Map<String, Object> updateInput = new HashMap<>();
        updateInput.put("id", "todo-1");
        updateInput.put("completed", true);
        Intent updateIntent = new Intent("updateTodo", updateInput, intentId2);

        ComputeResult updateResult = Compute.computeSync(todoSchema, afterAdd, updateIntent, 5);
        assertEquals(ComputeStatus.HALTED, updateResult.getStatus());

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
        assertTrue(computeResult.getStatus() == ComputeStatus.HALTED);

        // Step 3: 변경된 Snapshot 검증
        Snapshot resultSnapshot = computeResult.getSnapshot();
        assertValid(todoSchema, resultSnapshot);

        // Step 4: 추가 Patch 적용
        Patch patch = Patch.set("data.filter", "active");
        Result<Snapshot, ErrorValue> applyResult = Apply.apply(todoSchema, resultSnapshot, patch);
        assertTrue(applyResult.isOk());

        // Step 5: 최종 Snapshot 검증
        Snapshot finalSnapshot = applyResult.unwrap();
        assertValid(todoSchema, finalSnapshot);
        assertEquals(2, finalSnapshot.getMeta().getVersion());
    }

    @Test
    @DisplayName("Flow 패치 적용 + TraceGraph 검증")
    void testFlowPatchAndTraceGraph() throws Exception {
        // given: data.title을 업데이트하는 Flow
        ActionSpec updateTitle = new ActionSpec.Builder("updateTitle")
            .addInputField("title", FieldSpec.required("title", "string"))
            .flow(FlowNode.Patch.set("data.title", new Lit("Updated")))
            .build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:patch-test",
            "1.0.0",
            new ActionSpec[] { updateTitle },
            new FieldSpec[] { FieldSpec.required("title", "string") },
            new ComputedFieldDef[] { ComputedFieldDef.simple("noop", new Lit(0)) }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("title", "Original")))
            .computed(new HashMap<>(Map.of("noop", 0)))
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        Intent intent = new Intent("updateTitle", Map.of("title", "Updated"), UUID.randomUUID().toString());

        // when
        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        // then
        assertEquals(ComputeStatus.COMPLETE, result.getStatus());
        assertEquals("Updated", result.getSnapshot().getData().get("title"));
        assertNotNull(result.getTrace());
        assertEquals(result.getTrace().getRoot().getId(),
            result.getTrace().getNodes().get(result.getTrace().getRoot().getId()).getId());
    }

    @Test
    @DisplayName("Effect 선언 시 PENDING + Requirement 생성")
    void testEffectCreatesPendingRequirement() throws Exception {
        // given: 일반 effect
        ActionSpec effectAction = new ActionSpec.Builder("effectAction")
            .flow(FlowNode.Effect.of(
                "host.notify",
                Map.of("message", new Lit("hello"))
            ))
            .build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:effect-test",
            "1.0.0",
            new ActionSpec[] { effectAction },
            new FieldSpec[] { FieldSpec.required("title", "string") },
            new ComputedFieldDef[] { ComputedFieldDef.simple("noop", new Lit(0)) }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("title", "t")))
            .computed(new HashMap<>(Map.of("noop", 0)))
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        Intent intent = new Intent("effectAction", new HashMap<>(), UUID.randomUUID().toString());

        // when
        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        // then
        assertEquals(ComputeStatus.PENDING, result.getStatus());
        assertFalse(result.getRequirements().isEmpty());
        assertNotNull(result.getSnapshot().getSystem().getPendingRequirements());
    }

    @Test
    @DisplayName("배열 인라인 효과로 data 패치 적용")
    void testInlineArrayEffectPatch() throws Exception {
        // given: array.map effect
        ActionSpec mapAction = new ActionSpec.Builder("mapAction")
            .flow(FlowNode.Effect.of(
                "array.map",
                Map.of(
                    "source", new Lit(List.of(1, 2, 3)),
                    "into", new Lit("data.mapped"),
                    "select", new Lit(1)
                )
            ))
            .build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:array-map",
            "1.0.0",
            new ActionSpec[] { mapAction },
            new FieldSpec[] { new FieldSpec("mapped", "array", false, List.of()) },
            new ComputedFieldDef[] { ComputedFieldDef.simple("noop", new Lit(0)) }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("mapped", List.of())))
            .computed(new HashMap<>(Map.of("noop", 0)))
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        Intent intent = new Intent("mapAction", new HashMap<>(), UUID.randomUUID().toString());

        // when
        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        // then
        assertEquals(ComputeStatus.COMPLETE, result.getStatus());
        assertEquals(List.of(1, 1, 1), result.getSnapshot().getData().get("mapped"));
    }

    private void assertValid(DomainSchema schema, Snapshot snapshot) {
        Validate.ValidationResult result = Validate.validate(schema, snapshot);
        assertTrue(result.isValid(), result.errors().toString());
    }
}
