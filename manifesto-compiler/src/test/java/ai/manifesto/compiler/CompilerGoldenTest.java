package ai.manifesto.compiler;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Compiler Golden Tests")
class CompilerGoldenTest {

    @Test
    @DisplayName("MEL 컴파일 결과가 골든 기대값과 일치")
    void goldenCompilerCases() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("golden/compiler-e2e.json");
        assertFalse(vectors.isEmpty(), "Golden vectors should not be empty");

        MelCompiler compiler = new MelCompiler();

        for (Map<String, Object> vector : vectors) {
            String name = String.valueOf(vector.get("name"));
            String source = String.valueOf(vector.get("source"));
            Boolean expectSuccess = (Boolean) vector.getOrDefault("expectSuccess", Boolean.TRUE);
            Boolean expectHashDeterminism = (Boolean) vector.getOrDefault("expectHashDeterminism", Boolean.FALSE);

            CompilationResult result = compiler.compileDomain(source);

            if (!expectSuccess) {
                assertFalse(result.isOk(), "Expected compile failure: " + name);
                List<String> expectedErrors = castStringList(vector.get("expectedErrors"));
                if (expectedErrors != null && !expectedErrors.isEmpty()) {
                    List<String> actualCodes = result.getErrors().stream()
                        .map(d -> d.code().code())
                        .collect(Collectors.toList());
                    for (String code : expectedErrors) {
                        assertTrue(actualCodes.contains(code), "Missing error code " + code + " for " + name);
                    }
                }
                continue;
            }

            assertTrue(result.isOk(), "Expected compile success: " + name);

            if (expectHashDeterminism) {
                CompilationResult result2 = compiler.compileDomain(source);
                assertTrue(result2.isOk(), "Expected second compile success: " + name);
                assertEquals(result.getSchema().getHash(), result2.getSchema().getHash(), "Hash must be deterministic");
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> expected = (Map<String, Object>) vector.get("expected");
            assertNotNull(expected, "Expected golden data missing for: " + name);

            Map<String, Object> actual = normalizeSchema(result.getSchema(), expected);
            try {
                harness.assertJsonEquals(expected, actual);
            } catch (AssertionError error) {
                throw new AssertionError("Golden mismatch: " + name + "\n" + error.getMessage(), error);
            }
        }
    }

    private Map<String, Object> normalizeSchema(DomainSchema schema, Map<String, Object> expected) {
        Map<String, Object> out = new LinkedHashMap<>();

        if (expected.containsKey("id")) {
            out.put("id", schema.getId());
        }
        if (expected.containsKey("version")) {
            out.put("version", schema.getVersion());
        }
        if (expected.containsKey("dataFields")) {
            out.put("dataFields", sorted(schema.getDataFields().keySet()));
        }
        if (expected.containsKey("computedFields")) {
            out.put("computedFields", sorted(schema.getComputedFields().keySet()));
        }
        if (expected.containsKey("actions")) {
            out.put("actions", sorted(schema.getActions().keySet()));
        }
        if (expected.containsKey("actionInputs")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedInputs = (Map<String, Object>) expected.get("actionInputs");
            Map<String, Object> inputs = new LinkedHashMap<>();
            for (String actionId : expectedInputs.keySet()) {
                ActionSpec action = schema.getActions().get(actionId);
                List<String> fieldNames = action == null
                    ? List.of()
                    : sorted(action.getInputFields().keySet());
                inputs.put(actionId, fieldNames);
            }
            out.put("actionInputs", inputs);
        }
        if (expected.containsKey("dataFieldTypes")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedTypes = (Map<String, Object>) expected.get("dataFieldTypes");
            Map<String, Object> types = new LinkedHashMap<>();
            for (String fieldName : expectedTypes.keySet()) {
                FieldSpec spec = schema.getDataField(fieldName);
                if (spec == null) {
                    types.put(fieldName, null);
                    continue;
                }
                Map<String, Object> specMap = new LinkedHashMap<>();
                specMap.put("type", spec.getType());
                if (expectedTypeHas(expectedTypes, fieldName, "required")) {
                    specMap.put("required", spec.isRequired());
                }
                if (expectedTypeHas(expectedTypes, fieldName, "default")) {
                    specMap.put("default", spec.getDefaultValue());
                }
                if (expectedTypeHas(expectedTypes, fieldName, "enumValues")) {
                    specMap.put("enumValues", spec.getEnumValues());
                }
                types.put(fieldName, specMap);
            }
            out.put("dataFieldTypes", types);
        }
        if (expected.containsKey("computedExprKinds")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedKinds = (Map<String, Object>) expected.get("computedExprKinds");
            Map<String, Object> kinds = new LinkedHashMap<>();
            for (String fieldName : expectedKinds.keySet()) {
                ComputedFieldDef def = schema.getComputedField(fieldName);
                String kind = def == null ? null : simpleExprKind(def.getExpression());
                kinds.put(fieldName, kind);
            }
            out.put("computedExprKinds", kinds);
        }

        return out;
    }

    private boolean expectedTypeHas(Map<String, Object> expectedTypes, String fieldName, String key) {
        Object spec = expectedTypes.get(fieldName);
        if (!(spec instanceof Map<?, ?> map)) {
            return false;
        }
        return map.containsKey(key);
    }

    private String simpleExprKind(ExprNode expr) {
        return expr == null ? null : expr.getClass().getSimpleName();
    }

    private List<String> sorted(java.util.Collection<String> items) {
        return items.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object value) {
        if (value == null) return null;
        if (value instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object item : list) {
                out.add(String.valueOf(item));
            }
            return out;
        }
        return null;
    }
}
