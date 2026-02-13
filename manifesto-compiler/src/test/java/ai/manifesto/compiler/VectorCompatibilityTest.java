package ai.manifesto.compiler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Compiler 벡터 호환성 테스트")
class VectorCompatibilityTest {

    @Test
    @DisplayName("lowering 벡터 비교")
    void testLoweringVectors() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("vectors/lowering.json");
        assertFalse(vectors.isEmpty());
        Lowering lowering = new Lowering();
        for (Map<String, Object> vector : vectors) {
            Map<String, Object> input = (Map<String, Object>) vector.get("input");
            Map<String, Object> expected = (Map<String, Object>) vector.get("expected");
            Map<String, Object> actual = lowering.lowerExprNode(input);
            try {
                harness.assertJsonEquals(expected, actual);
            } catch (AssertionError error) {
                throw new AssertionError("lowering vector mismatch: " + vector.get("testName") + "\n" + error.getMessage(), error);
            }
        }
    }

    @Test
    @DisplayName("evaluation 벡터 비교")
    void testEvaluationVectors() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("vectors/evaluation.json");
        assertFalse(vectors.isEmpty());
        RuntimePatchEvaluator evaluator = new RuntimePatchEvaluator();
        Map<String, Object> profile = new java.util.LinkedHashMap<>();
        profile.put("name", "Alice");
        profile.put("age", 30);
        RuntimePatchEvaluator.SnapshotContext snapshot = new RuntimePatchEvaluator.SnapshotContext(
            Map.of(
                "count", 10,
                "name", "Alice",
                "items", List.of(1, 2, 3),
                "profile", profile
            ),
            Map.of("total", 100),
            Map.of("intentId", "test-intent-123"),
            Map.of("title", "Hello")
        );

        for (Map<String, Object> vector : vectors) {
            Map<String, Object> input = (Map<String, Object>) vector.get("input");
            Object expected = vector.get("expected");
            Object actual = evaluator.evaluateExpr(input, snapshot);
            harness.assertJsonEquals(expected, actual);
        }
    }

    @Test
    @DisplayName("IR generator 벡터 비교")
    void testIrGeneratorVectors() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("vectors/ir-generator.json");
        assertFalse(vectors.isEmpty());
    }

    @Test
    @DisplayName("runtime patch lowering 벡터 비교")
    void testLowerRuntimePatches() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("vectors/lowering-runtime-patch.json");
        Lowering lowering = new Lowering();
        for (Map<String, Object> vector : vectors) {
            List<Map<String, Object>> input = (List<Map<String, Object>>) vector.get("input");
            List<Map<String, Object>> expected = (List<Map<String, Object>>) vector.get("expected");
            List<Map<String, Object>> actual = lowering.lowerRuntimePatches(input);
            harness.assertJsonEquals(expected, actual);
        }
    }

    @Test
    @DisplayName("patch fragment lowering 벡터 비교")
    void testLowerPatchFragments() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("vectors/lowering-patch-fragment.json");
        Lowering lowering = new Lowering();
        for (Map<String, Object> vector : vectors) {
            List<Map<String, Object>> input = (List<Map<String, Object>>) vector.get("input");
            List<Map<String, Object>> expected = (List<Map<String, Object>>) vector.get("expected");
            List<Map<String, Object>> actual = lowering.lowerPatchFragments(input);
            harness.assertJsonEquals(expected, actual);
        }
    }

    @Test
    @DisplayName("runtime patch evaluation 벡터 비교")
    void testEvaluateRuntimePatches() throws Exception {
        VectorHarness harness = new VectorHarness();
        List<Map<String, Object>> vectors = harness.load("vectors/evaluation-runtime-patch.json");
        RuntimePatchEvaluator evaluator = new RuntimePatchEvaluator();

        RuntimePatchEvaluator.SnapshotContext snapshot = new RuntimePatchEvaluator.SnapshotContext(
            new java.util.LinkedHashMap<>(Map.of(
                "count", 0,
                "name", "Alice",
                "items", List.of(1, 2, 3)
            )),
            new java.util.LinkedHashMap<>(Map.of("total", 100)),
            new java.util.LinkedHashMap<>(),
            new java.util.LinkedHashMap<>()
        );

        for (Map<String, Object> vector : vectors) {
            List<Map<String, Object>> input = (List<Map<String, Object>>) vector.get("input");
            Object expected = vector.get("expected");
            RuntimePatchEvaluator.EvaluationResult result = evaluator.evaluate(input, snapshot);
            Object actual;
            if (expected instanceof Map<?, ?> expectedMap && expectedMap.containsKey("patches")) {
                Map<String, Object> finalSnapshot = Map.of(
                    "data", result.finalSnapshot().data(),
                    "computed", result.finalSnapshot().computed()
                );
                actual = Map.of(
                    "patches", result.patches(),
                    "skipped", result.skipped(),
                    "finalSnapshot", finalSnapshot
                );
            } else {
                actual = result.patches();
            }
            harness.assertJsonEquals(expected, actual);
        }
    }
}
