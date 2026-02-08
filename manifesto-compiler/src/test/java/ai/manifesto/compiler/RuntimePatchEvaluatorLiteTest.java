package ai.manifesto.compiler;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimePatchEvaluatorLiteTest {

    @Test
    void evaluateWithTraceRecordsAppliedSkippedAndDroppedEntries() {
        RuntimePatchEvaluatorLite evaluator = new RuntimePatchEvaluatorLite();
        RuntimePatchEvaluatorLite.SnapshotContext snapshot = new RuntimePatchEvaluatorLite.SnapshotContext(
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

        RuntimePatchEvaluatorLite.EvaluationTraceResult result = evaluator.evaluateWithTrace(patches, snapshot);

        assertEquals(1, result.patches().size());
        assertEquals(1, result.skipped().size());
        assertEquals("COND_FALSE", result.skipped().get(0).get("reason"));
        assertEquals(3, result.trace().size());
        assertTrue(result.trace().stream().anyMatch(e -> "applied".equals(e.get("event"))));
        assertTrue(result.trace().stream().anyMatch(e -> "skipped".equals(e.get("event"))));
        assertTrue(result.trace().stream().anyMatch(e -> "dropped".equals(e.get("event"))));
        assertEquals(1, result.finalSnapshot().data().get("count"));
    }

    @Test
    void evaluateStrictCollectsShapeErrors() {
        RuntimePatchEvaluatorLite evaluator = new RuntimePatchEvaluatorLite();
        RuntimePatchEvaluatorLite.SnapshotContext snapshot = new RuntimePatchEvaluatorLite.SnapshotContext(
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

        RuntimePatchEvaluatorLite.StrictEvaluationResult result = evaluator.evaluateStrict(patches, snapshot);

        assertEquals(1, result.patches().size());
        assertEquals(2, result.errors().size());
        assertTrue(result.errors().stream().anyMatch(e -> "RPV006".equals(e.get("code"))));
        assertTrue(result.errors().stream().anyMatch(e -> "RPV002".equals(e.get("code"))));
    }
}
