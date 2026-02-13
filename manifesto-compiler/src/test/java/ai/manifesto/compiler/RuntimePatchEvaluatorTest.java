package ai.manifesto.compiler;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimePatchEvaluatorTest {

    @Test
    void evaluateWithTraceRecordsAppliedSkippedAndDroppedEntries() {
        RuntimePatchEvaluator evaluator = new RuntimePatchEvaluator();
        RuntimePatchEvaluator.SnapshotContext snapshot = new RuntimePatchEvaluator.SnapshotContext(
                new LinkedHashMap<>(Map.of("count", 0)),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>()
        );

        Map<String, Object> conditionFalse = new LinkedHashMap<>();
        conditionFalse.put("kind", "lit");
        conditionFalse.put("value", false);

        Map<String, Object> valueOne = new LinkedHashMap<>();
        valueOne.put("kind", "lit");
        valueOne.put("value", 1);

        Map<String, Object> valueTwo = new LinkedHashMap<>();
        valueTwo.put("kind", "lit");
        valueTwo.put("value", 2);

        List<Map<String, Object>> patches = List.of(
                new LinkedHashMap<>(Map.of(
                        "op", "set",
                        "path", "count",
                        "value", valueOne,
                        "condition", conditionFalse
                )),
                new LinkedHashMap<>(Map.of(
                        "op", "set",
                        "path", "count",
                        "value", valueOne
                )),
                new LinkedHashMap<>(Map.of(
                        "op", "unknown",
                        "path", "count",
                        "value", valueTwo
                ))
        );

        RuntimePatchEvaluator.EvaluationTraceResult result = evaluator.evaluateWithTrace(patches, snapshot);

        assertEquals(1, result.patches().size());
        assertEquals(1, result.skipped().size());
        assertEquals("false", result.skipped().get(0).get("reason"));
        assertEquals(3, result.trace().size());
        assertTrue(result.trace().stream().anyMatch(e -> "applied".equals(e.get("event"))));
        assertTrue(result.trace().stream().anyMatch(e -> "skipped".equals(e.get("event"))));
        assertTrue(result.trace().stream().anyMatch(e -> "dropped".equals(e.get("event"))));
        assertEquals(1, result.finalSnapshot().data().get("count"));
    }

    @Test
    void evaluateStrictCollectsShapeErrors() {
        RuntimePatchEvaluator evaluator = new RuntimePatchEvaluator();
        RuntimePatchEvaluator.SnapshotContext snapshot = new RuntimePatchEvaluator.SnapshotContext(
                new LinkedHashMap<>(Map.of("count", 0)),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>()
        );

        Map<String, Object> validValue = new LinkedHashMap<>();
        validValue.put("kind", "lit");
        validValue.put("value", 1);

        List<Map<String, Object>> patches = List.of(
                new LinkedHashMap<>(Map.of("op", "set", "path", "count", "value", validValue)),
                new LinkedHashMap<>(Map.of("op", "set", "path", "count")),
                new LinkedHashMap<>(Map.of("op", "", "path", "count", "value", validValue))
        );

        RuntimePatchEvaluator.StrictEvaluationResult result = evaluator.evaluateStrict(patches, snapshot);

        assertEquals(1, result.patches().size());
        assertEquals(2, result.errors().size());
        assertTrue(result.errors().stream().anyMatch(e -> "RPV006".equals(e.get("code"))));
        assertTrue(result.errors().stream().anyMatch(e -> "RPV002".equals(e.get("code"))));
    }

    @Test
    void evaluateStrictRejectsUnsetWithValue() {
        RuntimePatchEvaluator evaluator = new RuntimePatchEvaluator();
        RuntimePatchEvaluator.SnapshotContext snapshot = new RuntimePatchEvaluator.SnapshotContext(
            new LinkedHashMap<>(Map.of("count", 0)),
            new LinkedHashMap<>(),
            new LinkedHashMap<>(),
            new LinkedHashMap<>()
        );

        Map<String, Object> validValue = new LinkedHashMap<>();
        validValue.put("kind", "lit");
        validValue.put("value", 1);

        List<Map<String, Object>> patches = List.of(
            new LinkedHashMap<>(Map.of("op", "unset", "path", "count", "value", validValue))
        );

        RuntimePatchEvaluator.StrictEvaluationResult result = evaluator.evaluateStrict(patches, snapshot);

        assertEquals(0, result.patches().size());
        assertEquals(1, result.errors().size());
        assertEquals("RPV008", result.errors().get(0).get("code"));
    }

    @Test
    void evaluateExprSupportsSubstringAndObjectOps() {
        RuntimePatchEvaluator evaluator = new RuntimePatchEvaluator();
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", "Alice");
        profile.put("age", 30);
        RuntimePatchEvaluator.SnapshotContext snapshot = new RuntimePatchEvaluator.SnapshotContext(
            new LinkedHashMap<>(Map.of(
                "profile", profile
            )),
            new LinkedHashMap<>(),
            new LinkedHashMap<>(),
            new LinkedHashMap<>()
        );

        Map<String, Object> substringExpr = new LinkedHashMap<>();
        substringExpr.put("kind", "substring");
        substringExpr.put("str", Map.of("kind", "lit", "value", "abcdef"));
        substringExpr.put("start", Map.of("kind", "lit", "value", 1));
        substringExpr.put("end", Map.of("kind", "lit", "value", 4));
        assertEquals("bcd", evaluator.evaluateExpr(substringExpr, snapshot));

        Map<String, Object> fieldExpr = new LinkedHashMap<>();
        fieldExpr.put("kind", "field");
        fieldExpr.put("object", Map.of("kind", "get", "path", "profile"));
        fieldExpr.put("property", "name");
        assertEquals("Alice", evaluator.evaluateExpr(fieldExpr, snapshot));

        Map<String, Object> keysExpr = new LinkedHashMap<>();
        keysExpr.put("kind", "keys");
        keysExpr.put("obj", Map.of("kind", "get", "path", "profile"));
        assertEquals(List.of("name", "age"), evaluator.evaluateExpr(keysExpr, snapshot));

        Map<String, Object> valuesExpr = new LinkedHashMap<>();
        valuesExpr.put("kind", "values");
        valuesExpr.put("obj", Map.of("kind", "get", "path", "profile"));
        assertEquals(List.of("Alice", 30), evaluator.evaluateExpr(valuesExpr, snapshot));

        Map<String, Object> entriesExpr = new LinkedHashMap<>();
        entriesExpr.put("kind", "entries");
        entriesExpr.put("obj", Map.of("kind", "get", "path", "profile"));
        assertEquals(List.of(List.of("name", "Alice"), List.of("age", 30)), evaluator.evaluateExpr(entriesExpr, snapshot));
    }

    @Test
    void evaluateExprAtSupportsRecordKeyLookup() {
        RuntimePatchEvaluator evaluator = new RuntimePatchEvaluator();
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", "Alice");
        RuntimePatchEvaluator.SnapshotContext snapshot = new RuntimePatchEvaluator.SnapshotContext(
            new LinkedHashMap<>(Map.of(
                "profile", profile
            )),
            new LinkedHashMap<>(),
            new LinkedHashMap<>(),
            new LinkedHashMap<>()
        );

        Map<String, Object> recordAtExpr = new LinkedHashMap<>();
        recordAtExpr.put("kind", "at");
        recordAtExpr.put("array", Map.of("kind", "get", "path", "profile"));
        recordAtExpr.put("index", Map.of("kind", "lit", "value", "name"));

        assertEquals("Alice", evaluator.evaluateExpr(recordAtExpr, snapshot));

        recordAtExpr.put("index", Map.of("kind", "lit", "value", "missing"));
        assertNull(evaluator.evaluateExpr(recordAtExpr, snapshot));
    }
}
