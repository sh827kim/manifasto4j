package ai.manifesto.core.core;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validate 검증 테스트")
class ValidateTest {

    private DomainSchema schema;
    private Snapshot snapshot;

    @BeforeEach
    void setUp() {
        schema = createBaseSchema();

        // 유효한 Snapshot 생성
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("age", 30);

        snapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();
    }

    @Test
    @DisplayName("유효한 Snapshot 검증 통과")
    void testValidSnapshot() {
        Validate.ValidationResult result = Validate.validate(schema, snapshot);

        assertTrue(result.isValid(), result.errors().toString());
        assertTrue(result.errors().isEmpty(), result.errors().toString());
    }

    @Test
    @DisplayName("isValid 편의 메서드")
    void testIsValid() {
        Validate.ValidationResult result = Validate.validate(schema, snapshot);
        assertTrue(result.isValid(), result.errors().toString());
    }

    @Test
    @DisplayName("null Snapshot 검증 실패")
    void testNullSnapshot() {
        assertThrows(NullPointerException.class, () -> {
            Validate.validate(schema, null);
        });
    }

    @Test
    @DisplayName("null Schema 검증 실패")
    void testNullSchema() {
        assertThrows(NullPointerException.class, () -> {
            Validate.validate(null, snapshot);
        });
    }

    @Test
    @DisplayName("필수 필드 누락 검증")
    void testMissingRequiredField() {
        Map<String, Object> incompleteData = new HashMap<>();
        incompleteData.put("age", 25);
        // 필수 필드인 "name"이 없음

        Snapshot invalidSnapshot = snapshot.withData(incompleteData);
        Validate.ValidationResult result = Validate.validate(schema, invalidSnapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream()
            .anyMatch(e -> e.contains("name"))
        );
    }

    @Test
    @DisplayName("Snapshot 구조 검증")
    void testSnapshotStructure() {
        // 모든 필요한 필드가 있는 Snapshot은 유효함
        Validate.ValidationResult result = Validate.validate(schema, snapshot);
        assertTrue(result.isValid(), result.errors().toString());

        // data가 null인 경우
        Map<String, Object> emptyData = new HashMap<>();
        Snapshot noDataSnapshot = Snapshot.builder()
            .data(null)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();
        assertFalse(Validate.isValid(schema, noDataSnapshot));
    }

    @Test
    @DisplayName("System 필드 검증")
    void testSystemFieldValidation() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test");

        // Snapshot 생성자는 null system을 자동으로 초기화합니다
        // 따라서 null system을 검증하는 것은 의미가 없습니다
        // 대신 유효한 system을 가진 Snapshot이 유효함을 확인합니다
        Snapshot testSnapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();

        Validate.ValidationResult result = Validate.validate(schema, testSnapshot);
        assertTrue(result.isValid(), result.errors().toString());
    }

    @Test
    @DisplayName("Input 필드 유효성 검증")
    void testInputFieldValidation() {
        Map<String, Object> input = new HashMap<>();
        input.put("valid_field", "value");
        input.put("another_field", 123);

        Snapshot testSnapshot = snapshot.withInput(input);
        Validate.ValidationResult result = Validate.validate(schema, testSnapshot);

        assertTrue(result.isValid(), result.errors().toString());
    }

    @Test
    @DisplayName("유효하지 않은 필드명 검증")
    void testInvalidFieldName() {
        Map<String, Object> input = new HashMap<>();
        input.put("invalid-field", "value"); // 하이픈은 유효하지 않음

        Snapshot testSnapshot = snapshot.withInput(input);
        Validate.ValidationResult result = Validate.validate(schema, testSnapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream()
            .anyMatch(e -> e.contains("invalid-field") || e.contains("identifier"))
        );
    }

    @Test
    @DisplayName("버전 검증")
    void testVersionValidation() {
        Snapshot negativeVersionSnapshot = snapshot.withMeta(
            Snapshot.SnapshotMeta.create(-1, System.currentTimeMillis(), "seed", "hash")
        );

        Validate.ValidationResult result = Validate.validate(schema, negativeVersionSnapshot);
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("타임스탬프 검증")
    void testTimestampValidation() {
        Snapshot zeroTimestampSnapshot = snapshot.withMeta(
            Snapshot.SnapshotMeta.create(0, 0, "seed", "hash")
        );

        Validate.ValidationResult result = Validate.validate(schema, zeroTimestampSnapshot);
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("ValidationResult toString")
    void testValidationResultToString() {
        Validate.ValidationResult validResult = Validate.validate(schema, snapshot);
        String output = validResult.toString();
        assertTrue(output.contains("valid"));

        Validate.ValidationResult invalidResult = Validate.ValidationResult.invalid("test error");
        String errorOutput = invalidResult.toString();
        assertTrue(errorOutput.contains("error") || errorOutput.contains("invalid"));
    }

    @Test
    @DisplayName("스키마 해시 불일치 검증")
    void testSchemaHashMismatch() {
        DomainSchema invalidHashSchema = new DomainSchema.Builder("test-schema", "1.0.0")
            .hash("wrong-hash")
            .addDataField(FieldSpec.required("name", "string"))
            .addDataField(new FieldSpec("age", "integer", false, 0))
            .addComputedField(new ComputedFieldDef.Builder("greeting", new Get("data.name"))
                .addDependency("data.name")
                .build())
            .addAction(new ActionSpec.Builder("noop")
                .flow(FlowNode.Halt.of(null))
                .build())
            .build();

        Validate.ValidationResult result = Validate.validate(invalidHashSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("V-008")));
    }

    @Test
    @DisplayName("Computed deps 경로 존재 검증")
    void testComputedDepsMissingPath() {
        ComputedFieldDef computed = new ComputedFieldDef.Builder("total", new Get("data.name"))
            .addDependency("data.name")
            .addDependency("data.missing")
            .build();

        DomainSchema invalidSchema = buildSchemaWithHash(
            "test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string"), new FieldSpec("age", "integer", false, 0)),
            List.of(computed),
            List.of(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build())
        );

        Validate.ValidationResult result = Validate.validate(invalidSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("V-001") && e.contains("Unknown dependency")));
    }

    @Test
    @DisplayName("Computed 순환 참조 V-002 검증")
    void testComputedCycleValidation() {
        ComputedFieldDef fieldA = new ComputedFieldDef.Builder("a", new Get("computed.b"))
            .addDependency("b")
            .build();
        ComputedFieldDef fieldB = new ComputedFieldDef.Builder("b", new Get("computed.a"))
            .addDependency("a")
            .build();

        DomainSchema invalidSchema = buildSchemaWithHash(
            "test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string"), new FieldSpec("age", "integer", false, 0)),
            List.of(fieldA, fieldB),
            List.of(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build())
        );

        Validate.ValidationResult result = Validate.validate(invalidSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("V-002") && e.contains("a")));
    }

    @Test
    @DisplayName("Computed 표현식에서 input 경로 차단")
    void testComputedExprInputPath() {
        ComputedFieldDef computed = new ComputedFieldDef.Builder("fromInput", new Get("input.title"))
            .addDependency("data.name")
            .build();

        DomainSchema invalidSchema = buildSchemaWithHash(
            "test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string"), new FieldSpec("age", "integer", false, 0)),
            List.of(computed),
            List.of(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build())
        );

        Validate.ValidationResult result = Validate.validate(invalidSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("V-003") && e.contains("input path")));
    }

    @Test
    @DisplayName("Action 입력 스키마 없는 input 경로 검증")
    void testActionExprInputPathWithoutSpec() {
        ActionSpec action = new ActionSpec.Builder("usesInput")
            .available(new Get("input.title"))
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema invalidSchema = buildSchemaWithHash(
            "test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string"), new FieldSpec("age", "integer", false, 0)),
            List.of(new ComputedFieldDef.Builder("greeting", new Get("data.name"))
                .addDependency("data.name")
                .build()),
            List.of(action)
        );

        Validate.ValidationResult result = Validate.validate(invalidSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("V-003") && e.contains("Unknown input path")));
    }

    @Test
    @DisplayName("Flow call 참조 존재 검증")
    void testCallReferenceValidation() {
        ActionSpec action = new ActionSpec.Builder("caller")
            .flow(FlowNode.Call.of("missing"))
            .build();

        DomainSchema invalidSchema = buildSchemaWithHash(
            "test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string"), new FieldSpec("age", "integer", false, 0)),
            List.of(new ComputedFieldDef.Builder("greeting", new Get("data.name"))
                .addDependency("data.name")
                .build()),
            List.of(action)
        );

        Validate.ValidationResult result = Validate.validate(invalidSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("V-004")));
    }

    @Test
    @DisplayName("Flow call 그래프 순환 검증")
    void testCallGraphCycle() {
        ActionSpec actionA = new ActionSpec.Builder("actionA")
            .flow(FlowNode.Call.of("actionB"))
            .build();
        ActionSpec actionB = new ActionSpec.Builder("actionB")
            .flow(FlowNode.Call.of("actionA"))
            .build();

        DomainSchema invalidSchema = buildSchemaWithHash(
            "test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string"), new FieldSpec("age", "integer", false, 0)),
            List.of(new ComputedFieldDef.Builder("greeting", new Get("data.name"))
                .addDependency("data.name")
                .build()),
            List.of(actionA, actionB)
        );

        Validate.ValidationResult result = Validate.validate(invalidSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("V-005")));
    }

    @Test
    @DisplayName("스키마 id/semver 검증")
    void testSchemaIdAndSemverValidation() {
        DomainSchema invalidSchema = buildSchemaWithHash(
            "not-a-valid-id",
            "1",
            List.of(FieldSpec.required("name", "string"), new FieldSpec("age", "integer", false, 0)),
            List.of(new ComputedFieldDef.Builder("greeting", new Get("data.name"))
                .addDependency("data.name")
                .build()),
            List.of(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build())
        );

        Validate.ValidationResult result = Validate.validate(invalidSchema, snapshot);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Schema id")));
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Schema version")));
    }

    private DomainSchema createBaseSchema() {
        FieldSpec nameField = FieldSpec.required("name", "string");
        FieldSpec ageField = new FieldSpec("age", "integer", false, 0);

        ComputedFieldDef computed = new ComputedFieldDef.Builder("greeting", new Get("data.name"))
            .addDependency("data.name")
            .build();

        ActionSpec action = new ActionSpec.Builder("noop")
            .flow(FlowNode.Halt.of(null))
            .build();

        return buildSchemaWithHash(
            "urn:test-schema",
            "1.0.0",
            List.of(nameField, ageField),
            List.of(computed),
            List.of(action)
        );
    }

    private DomainSchema buildSchemaWithHash(
        String id,
        String version,
        List<FieldSpec> dataFields,
        List<ComputedFieldDef> computedFields,
        List<ActionSpec> actions
    ) {
        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(id, version);
        applySchemaFields(tempBuilder, dataFields, computedFields, actions);
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        DomainSchema.Builder builder = new DomainSchema.Builder(id, version);
        applySchemaFields(builder, dataFields, computedFields, actions);
        return builder.hash(hash).build();
    }

    private void applySchemaFields(
        DomainSchema.Builder builder,
        List<FieldSpec> dataFields,
        List<ComputedFieldDef> computedFields,
        List<ActionSpec> actions
    ) {
        for (FieldSpec field : dataFields) {
            builder.addDataField(field);
        }
        for (ComputedFieldDef computed : computedFields) {
            builder.addComputedField(computed);
        }
        for (ActionSpec action : actions) {
            builder.addAction(action);
        }
    }
}
