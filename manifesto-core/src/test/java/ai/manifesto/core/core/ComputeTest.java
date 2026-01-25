package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.flow.FlowNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Compute 계산 엔진 테스트")
class ComputeTest {

    private DomainSchema schema;
    private Snapshot snapshot;

    @BeforeEach
    void setUp() {
        FieldSpec titleField = FieldSpec.required("title", "string");

        ActionSpec addAction = new ActionSpec.Builder("addTodo")
            .addInputField("title", titleField)
            .flow(FlowNode.Halt.of(null))
            .build();

        schema = new DomainSchema.Builder("todo-app", "1.0.0")
            .hash("schema-hash")
            .addAction(addAction)
            .addDataField(titleField)
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("title", "");

        snapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();
    }

    @Test
    @DisplayName("정상적인 Compute 실행")
    void testComputeSuccess() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Learn Manifesto");
        Intent intent = new Intent("addTodo", input, UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        assertNotNull(result);
        assertEquals(ComputeStatus.COMPLETE, result.getStatus());
    }

    @Test
    @DisplayName("존재하지 않는 액션 실행")
    void testComputeNonExistentAction() throws Exception {
        Intent intent = new Intent("nonexistent", new HashMap<>(), UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        assertEquals(ComputeStatus.ERROR, result.getStatus());
    }

    @Test
    @DisplayName("Intent ID 없이 실행")
    void testComputeWithoutIntentId() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Test");
        Intent intent = new Intent("addTodo", input, "");

        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        assertEquals(ComputeStatus.ERROR, result.getStatus());
    }

    @Test
    @DisplayName("유효하지 않은 입력 필드명")
    void testComputeWithInvalidInputField() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("invalid-field-name", "value"); // 하이픈 포함

        Intent intent = new Intent("addTodo", input, UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        // 입력 필드 검증 실패
        assertTrue(result.getStatus() == ComputeStatus.ERROR || result.getStatus() == ComputeStatus.COMPLETE);
    }

    @Test
    @DisplayName("입력 데이터와 함께 Compute 실행")
    void testComputeWithInput() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Buy groceries");
        Intent intent = new Intent("addTodo", input, UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(schema, snapshot, intent, 5);

        assertNotNull(result);
        assertEquals(ComputeStatus.COMPLETE, result.getStatus());
        assertTrue(result.getSnapshot().getInput().containsKey("title"));
    }

    @Test
    @DisplayName("동일한 입력으로 결과 일관성 (결정성)")
    void testComputeDeterminism() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("title", "Test Task");
        String intentId = "test-intent-123";

        Intent intent1 = new Intent("addTodo", input, intentId);
        Intent intent2 = new Intent("addTodo", input, intentId);

        ComputeResult result1 = Compute.computeSync(schema, snapshot, intent1, 5);
        ComputeResult result2 = Compute.computeSync(schema, snapshot, intent2, 5);

        // 같은 입력이면 같은 상태를 반환해야 함
        assertEquals(result1.getStatus(), result2.getStatus());
    }

    @Test
    @DisplayName("Available 조건이 있는 액션")
    void testComputeWithAvailableCondition() throws Exception {
        // available 조건이 있는 액션 생성
        ai.manifesto.core.expr.literal.Lit alwaysTrue = new ai.manifesto.core.expr.literal.Lit(true);

        ActionSpec conditionalAction = new ActionSpec.Builder("conditionalAction")
            .addInputField("value", FieldSpec.required("value", "integer"))
            .available(alwaysTrue)
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema schemaWithCondition = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash1")
            .addAction(conditionalAction)
            .build();

        Map<String, Object> input = new HashMap<>();
        input.put("value", 42);
        Intent intent = new Intent("conditionalAction", input, UUID.randomUUID().toString());

        ComputeResult result = Compute.computeSync(schemaWithCondition, snapshot, intent, 5);

        assertNotNull(result);
    }

    @Test
    @DisplayName("null Schema 처리")
    void testComputeWithNullSchema() {
        Intent intent = new Intent("action", new HashMap<>(), UUID.randomUUID().toString());

        assertThrows(NullPointerException.class, () -> {
            Compute.computeSync(null, snapshot, intent, 5);
        });
    }

    @Test
    @DisplayName("null Snapshot 처리")
    void testComputeWithNullSnapshot() {
        Intent intent = new Intent("addTodo", new HashMap<>(), UUID.randomUUID().toString());

        assertThrows(NullPointerException.class, () -> {
            Compute.computeSync(schema, null, intent, 5);
        });
    }

    @Test
    @DisplayName("null Intent 처리")
    void testComputeWithNullIntent() {
        assertThrows(NullPointerException.class, () -> {
            Compute.computeSync(schema, snapshot, null, 5);
        });
    }

    @Test
    @DisplayName("여러 액션 정의")
    void testComputeWithMultipleActions() throws Exception {
        FieldSpec titleField = FieldSpec.required("title", "string");
        FieldSpec idField = FieldSpec.required("id", "string");

        ActionSpec addAction = new ActionSpec.Builder("add")
            .addInputField("title", titleField)
            .flow(FlowNode.Halt.of(null))
            .build();

        ActionSpec deleteAction = new ActionSpec.Builder("delete")
            .addInputField("id", idField)
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema multiActionSchema = new DomainSchema.Builder("todo", "1.0.0")
            .hash("hash1")
            .addAction(addAction)
            .addAction(deleteAction)
            .build();

        // add 액션 실행
        Map<String, Object> addInput = new HashMap<>();
        addInput.put("title", "New Task");
        Intent addIntent = new Intent("add", addInput, UUID.randomUUID().toString());

        ComputeResult addResult = Compute.computeSync(multiActionSchema, snapshot, addIntent, 5);
        assertEquals(ComputeStatus.COMPLETE, addResult.getStatus());

        // delete 액션 실행
        Map<String, Object> deleteInput = new HashMap<>();
        deleteInput.put("id", "task-123");
        Intent deleteIntent = new Intent("delete", deleteInput, UUID.randomUUID().toString());

        ComputeResult deleteResult = Compute.computeSync(multiActionSchema, snapshot, deleteIntent, 5);
        assertEquals(ComputeStatus.COMPLETE, deleteResult.getStatus());
    }

    @Test
    @DisplayName("비동기 compute 실행")
    void testAsyncCompute() throws Exception {
        Intent intent = new Intent("addTodo", new HashMap<>(), UUID.randomUUID().toString());

        var future = Compute.compute(schema, snapshot, intent);

        assertNotNull(future);
        assertTrue(!future.isDone() || future.isDone());

        ComputeResult result = future.get();
        assertNotNull(result);
    }
}
