package ai.manifesto.compiler;

import ai.manifesto.core.schema.FieldSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("IR Generator 벡터 테스트")
class IrGeneratorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("ir-generator 벡터 비교")
    void testIrVectors() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("vectors/ir-generator.json");
        IrGenerator generator = new IrGenerator();

        for (Map<String, Object> vector : vectors) {
            Map<String, Object> input = (Map<String, Object>) vector.get("input");
            Map<String, Object> expected = (Map<String, Object>) vector.get("expected");
            Map<String, Object> expectedFieldSpec = (Map<String, Object>) expected.get("fieldSpec");
            Map<String, Object> actualFieldSpec;

            if (input.containsKey("typeExpr")) {
                FieldSpec spec = generator.generateFieldSpec("value", String.valueOf(input.get("typeExpr")));
                actualFieldSpec = toFieldSpecMap(spec);
            } else if (input.containsKey("params")) {
                Map<String, Object> params = (Map<String, Object>) input.get("params");
                String payloadSpec = String.valueOf(params.get("payload"));
                Map<String, FieldSpec> inputFields = generator.generateInputFields("payload:" + payloadSpec);
                FieldSpec spec = inputFields.get("payload");
                assertNotNull(spec);
                actualFieldSpec = toFieldSpecMap(spec);
            } else {
                throw new IllegalArgumentException("Unknown vector input: " + vector.get("testName"));
            }

            harness.assertJsonEquals(expectedFieldSpec, actualFieldSpec);
        }
    }

    private Map<String, Object> toFieldSpecMap(FieldSpec spec) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (spec.getEnumValues() != null && !spec.getEnumValues().isEmpty()) {
            result.put("type", Map.of("enum", spec.getEnumValues()));
        } else {
            result.put("type", spec.getType());
        }
        result.put("required", spec.isRequired());
        if (spec.getFields() != null && !spec.getFields().isEmpty()) {
            Map<String, Object> fields = new LinkedHashMap<>();
            for (Map.Entry<String, FieldSpec> entry : spec.getFields().entrySet()) {
                fields.put(entry.getKey(), toFieldSpecMap(entry.getValue()));
            }
            result.put("fields", fields);
        }
        if (spec.getItems() != null) {
            result.put("items", toFieldSpecMap(spec.getItems()));
        }
        return result;
    }
}
