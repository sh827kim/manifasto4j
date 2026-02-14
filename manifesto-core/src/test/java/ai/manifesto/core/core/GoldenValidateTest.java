package ai.manifesto.core.core;

import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Core Golden Validate Tests")
class GoldenValidateTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Validate 결과가 골든 기대값과 일치")
    void goldenValidateCases() throws Exception {
        List<Map<String, Object>> vectors = loadVectors("golden/validate.json");
        assertFalse(vectors.isEmpty(), "Golden vectors should not be empty");

        for (Map<String, Object> vector : vectors) {
            String name = String.valueOf(vector.get("name"));
            @SuppressWarnings("unchecked")
            Map<String, Object> expected = (Map<String, Object>) vector.get("expected");
            assertNotNull(expected, "Expected golden data missing for: " + name);

            Validate.ValidationResult result = switch (name) {
                case "v002-computed-cycle" -> Validate.validateSchema(createComputedCycleSchema());
                case "v004-unknown-call-reference" -> Validate.validateSchema(createUnknownCallReferenceSchema());
                case "v005-call-graph-cycle" -> Validate.validateSchema(createCallGraphCycleSchema());
                case "v008-schema-hash-mismatch" -> Validate.validateSchema(createSchemaHashMismatchSchema());
                default -> throw new IllegalArgumentException("Unknown validate golden case: " + name);
            };

            Map<String, Object> actual = normalize(result, expected);
            assertJsonEquals(expected, actual, "Golden mismatch: " + name);
        }
    }

    private DomainSchema createComputedCycleSchema() {
        ComputedFieldDef fieldA = new ComputedFieldDef.Builder("computed.a", new Get("computed.b"))
            .addDependency("computed.b")
            .build();
        ComputedFieldDef fieldB = new ComputedFieldDef.Builder("computed.b", new Get("computed.a"))
            .addDependency("computed.a")
            .build();

        return buildSchemaWithHash(
            "urn:test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string")),
            List.of(fieldA, fieldB),
            List.of(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build())
        );
    }

    private DomainSchema createCallGraphCycleSchema() {
        ActionSpec actionA = new ActionSpec.Builder("actionA")
            .flow(FlowNode.Call.of("actionB"))
            .build();
        ActionSpec actionB = new ActionSpec.Builder("actionB")
            .flow(FlowNode.Call.of("actionA"))
            .build();
        ComputedFieldDef computed = new ComputedFieldDef.Builder("computed.name", new Get("name"))
            .addDependency("name")
            .build();

        return buildSchemaWithHash(
            "urn:test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string")),
            List.of(computed),
            List.of(actionA, actionB)
        );
    }

    private DomainSchema createUnknownCallReferenceSchema() {
        ActionSpec actionA = new ActionSpec.Builder("actionA")
            .flow(FlowNode.Call.of("missingAction"))
            .build();
        ComputedFieldDef computed = new ComputedFieldDef.Builder("computed.name", new Get("name"))
            .addDependency("name")
            .build();

        return buildSchemaWithHash(
            "urn:test-schema",
            "1.0.0",
            List.of(FieldSpec.required("name", "string")),
            List.of(computed),
            List.of(actionA)
        );
    }

    private DomainSchema createSchemaHashMismatchSchema() {
        return new DomainSchema.Builder("urn:test-schema", "1.0.0")
            .hash("wrong-hash")
            .addDataField(FieldSpec.required("name", "string"))
            .addComputedField(new ComputedFieldDef.Builder("computed.name", new Get("name"))
                .addDependency("name")
                .build())
            .addAction(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build())
            .build();
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

    private Map<String, Object> normalize(Validate.ValidationResult result, Map<String, Object> expected) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (expected.containsKey("isValid")) {
            out.put("isValid", result.isValid());
        }
        if (expected.containsKey("errorCodes")) {
            List<String> codes = new ArrayList<>();
            for (Validate.ValidationError error : result.errors()) {
                codes.add(error.code());
            }
            codes = codes.stream().distinct().sorted().toList();
            out.put("errorCodes", codes);
        }
        return out;
    }

    private List<Map<String, Object>> loadVectors(String resourcePath) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing resource: " + resourcePath);
            }
            return mapper.readValue(input, new TypeReference<>() {});
        }
    }

    private void assertJsonEquals(Object expected, Object actual, String message) throws Exception {
        JsonNode expectedNode = mapper.valueToTree(expected);
        JsonNode actualNode = mapper.valueToTree(actual);
        if (!expectedNode.equals(actualNode)) {
            throw new AssertionError(message + "\nExpected: " + expectedNode + "\nActual: " + actualNode);
        }
    }
}
