package ai.manifesto.core.schema;

import ai.manifesto.core.flow.FlowNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DomainSchema 테스트")
class DomainSchemaTest {

    @Test
    @DisplayName("기본 스키마 생성")
    void testBasicSchema() {
        DomainSchema schema = new DomainSchema.Builder("todo-app", "1.0.0")
            .hash("abc123")
            .build();

        assertEquals("todo-app", schema.getId());
        assertEquals("1.0.0", schema.getVersion());
        assertEquals("abc123", schema.getHash());
    }

    @Test
    @DisplayName("필드 추가")
    void testAddDataFields() {
        FieldSpec titleField = FieldSpec.required("title", "string");
        FieldSpec completedField = new FieldSpec("completed", "boolean", false, false);

        DomainSchema schema = new DomainSchema.Builder("todo", "1.0.0")
            .hash("hash1")
            .addDataField(titleField)
            .addDataField(completedField)
            .build();

        Map<String, FieldSpec> fields = schema.getDataFields();
        assertEquals(2, fields.size());
        assertTrue(fields.containsKey("title"));
        assertTrue(fields.containsKey("completed"));
    }

    @Test
    @DisplayName("액션 추가")
    void testAddActions() {
        ActionSpec addAction = new ActionSpec.Builder("addTodo")
            .addInputField("title", FieldSpec.required("title", "string"))
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema schema = new DomainSchema.Builder("todo", "1.0.0")
            .hash("hash1")
            .addAction(addAction)
            .build();

        Map<String, ActionSpec> actions = schema.getActions();
        assertEquals(1, actions.size());
        assertTrue(actions.containsKey("addTodo"));
        assertEquals(addAction, schema.getAction("addTodo"));
    }

    @Test
    @DisplayName("스키마에서 액션 조회")
    void testGetAction() {
        ActionSpec updateAction = new ActionSpec.Builder("updateTodo")
            .addInputField("id", FieldSpec.required("id", "string"))
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema schema = new DomainSchema.Builder("todo", "1.0.0")
            .hash("hash1")
            .addAction(updateAction)
            .build();

        ActionSpec retrieved = schema.getAction("updateTodo");
        assertNotNull(retrieved);
        assertEquals(updateAction, retrieved);
    }

    @Test
    @DisplayName("존재하지 않는 액션 조회")
    void testGetNonExistentAction() {
        DomainSchema schema = new DomainSchema.Builder("todo", "1.0.0")
            .hash("hash1")
            .build();

        ActionSpec action = schema.getAction("nonexistent");
        assertNull(action);
    }

    @Test
    @DisplayName("여러 필드와 액션 추가")
    void testMultipleFieldsAndActions() {
        FieldSpec idField = FieldSpec.required("id", "string");
        FieldSpec titleField = FieldSpec.required("title", "string");
        FieldSpec completedField = new FieldSpec("completed", "boolean", false, false);

        ActionSpec addAction = new ActionSpec.Builder("add")
            .addInputField("title", titleField)
            .flow(FlowNode.Halt.of(null))
            .build();

        ActionSpec updateAction = new ActionSpec.Builder("update")
            .addInputField("id", idField)
            .addInputField("completed", completedField)
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema schema = new DomainSchema.Builder("todo", "2.0.0")
            .hash("hash2")
            .addDataField(idField)
            .addDataField(titleField)
            .addDataField(completedField)
            .addAction(addAction)
            .addAction(updateAction)
            .build();

        assertEquals(3, schema.getDataFields().size());
        assertEquals(2, schema.getActions().size());
    }

    @Test
    @DisplayName("FieldSpec required 생성")
    void testFieldSpecRequired() {
        FieldSpec field = FieldSpec.required("email", "string");

        assertTrue(field.isRequired());
        assertNotNull(field);
    }

    @Test
    @DisplayName("FieldSpec optional 생성")
    void testFieldSpecOptional() {
        FieldSpec field = new FieldSpec("nickname", "string", false, false);

        assertFalse(field.isRequired());
        assertNotNull(field);
    }

    @Test
    @DisplayName("ActionSpec Builder")
    void testActionSpecBuilder() {
        ActionSpec action = new ActionSpec.Builder("createUser")
            .addInputField("name", FieldSpec.required("name", "string"))
            .addInputField("email", FieldSpec.required("email", "string"))
            .flow(FlowNode.Halt.of(null))
            .build();

        assertEquals(2, action.getInputFields().size());
        assertNotNull(action.getFlow());
    }

    @Test
    @DisplayName("스키마 버전 관리")
    void testSchemaVersioning() {
        DomainSchema v1 = new DomainSchema.Builder("app", "1.0.0")
            .hash("hash-v1")
            .build();

        DomainSchema v2 = new DomainSchema.Builder("app", "1.1.0")
            .hash("hash-v1-1")
            .build();

        assertEquals("1.0.0", v1.getVersion());
        assertEquals("1.1.0", v2.getVersion());
        assertNotEquals(v1.getHash(), v2.getHash());
    }

    @Test
    @DisplayName("ComputedFieldDef 생성")
    void testComputedFieldDef() {
        ai.manifesto.core.expr.literal.Lit expr = new ai.manifesto.core.expr.literal.Lit(100);

        ComputedFieldDef computed = ComputedFieldDef.simple("total", expr);

        assertEquals("total", computed.getFieldName());
        assertNotNull(computed.getExpression());
        assertTrue(computed.getDependencies().isEmpty());
    }

    @Test
    @DisplayName("스키마에 computed 필드 추가")
    void testAddComputedFields() {
        ai.manifesto.core.expr.literal.Lit expr = new ai.manifesto.core.expr.literal.Lit(42);
        ComputedFieldDef computed = ComputedFieldDef.simple("answer", expr);

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash1")
            .addComputedField(computed)
            .build();

        Map<String, ComputedFieldDef> computedFields = schema.getComputedFields();
        assertEquals(1, computedFields.size());
        assertTrue(computedFields.containsKey("answer"));
    }
}
