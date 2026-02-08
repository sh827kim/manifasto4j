package ai.manifesto.compiler;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoweringLiteStrictTest {

    @Test
    void lowerRuntimePatchesStrictRejectsMissingValueForSet() {
        LoweringLite lowering = new LoweringLite();
        List<Map<String, Object>> patches = List.of(
            new LinkedHashMap<>(Map.of("op", "set", "path", "count"))
        );

        LoweringError error = assertThrows(
            LoweringError.class,
            () -> lowering.lowerRuntimePatchesStrict(patches)
        );
        assertEquals(LoweringErrorCode.INVALID_SHAPE, error.getCode());
    }

    @Test
    void lowerRuntimePatchesStrictAcceptsValidSetUnsetMerge() {
        LoweringLite lowering = new LoweringLite();
        Map<String, Object> litOne = new LinkedHashMap<>();
        litOne.put("kind", "lit");
        litOne.put("value", 1);
        Map<String, Object> mergeValue = new LinkedHashMap<>();
        mergeValue.put("kind", "obj");
        mergeValue.put("fields", List.of());

        List<Map<String, Object>> patches = List.of(
            new LinkedHashMap<>(Map.of("op", "set", "path", "count", "value", litOne)),
            new LinkedHashMap<>(Map.of("op", "unset", "path", "count")),
            new LinkedHashMap<>(Map.of("op", "merge", "path", "profile", "value", mergeValue))
        );

        List<Map<String, Object>> lowered = lowering.lowerRuntimePatchesStrict(patches);
        assertEquals(3, lowered.size());
    }
}
