package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.expr.arithmetic.Add;
import ai.manifesto.core.expr.type.Coalesce;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Core Golden Compute Tests")
class GoldenComputeTest {

    private static final long NOW = 1000L;
    private static final String SEED = "seed";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Compute 결과가 골든 기대값과 일치")
    void goldenComputeCases() throws Exception {
        List<Map<String, Object>> vectors = loadVectors("golden/compute.json");
        assertFalse(vectors.isEmpty(), "Golden vectors should not be empty");

        for (Map<String, Object> vector : vectors) {
            String name = String.valueOf(vector.get("name"));
            Map<String, Object> expected = castMap(vector.get("expected"));
            assertNotNull(expected, "Expected data missing for: " + name);

            ComputeResult result = switch (name) {
                case "increment" -> runIncrementCase();
                case "unknown-action" -> runUnknownActionCase();
                case "effect-pending" -> runEffectPendingCase();
                case "meta-intent" -> runMetaIntentCase();
                default -> throw new IllegalArgumentException("Unknown golden case: " + name);
            };

            Map<String, Object> actual = normalize(result, expected);
            assertJsonEquals(expected, actual, "Golden mismatch: " + name);
        }
    }

    private ComputeResult runIncrementCase() throws Exception {
        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash")
            .addDataField(FieldSpec.required("count", "number"))
            .addAction(new ActionSpec.Builder("increment")
                .flow(FlowNode.Patch.set(
                    "count",
                    new Add(
                        new Coalesce(List.of(new Get("count"), new Lit(0))),
                        new Lit(1)
                    )
                ))
                .build())
            .build();

        Snapshot snapshot = Snapshot.builder()
            .data(new LinkedHashMap<>(Map.of("count", 0)))
            .computed(new LinkedHashMap<>())
            .system(SystemState.initial())
            .input(new LinkedHashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, NOW, SEED, schema.getHash()))
            .build();

        Intent intent = new Intent("increment", Map.of(), "intent-1");
        HostContext context = HostContext.builder(NOW, SEED).durationMs(0L).build();
        return Compute.computeSync(schema, snapshot, intent, context, 5);
    }

    private ComputeResult runUnknownActionCase() throws Exception {
        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash")
            .addAction(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of("noop")).build())
            .build();

        Snapshot snapshot = Snapshot.builder()
            .data(new LinkedHashMap<>())
            .computed(new LinkedHashMap<>())
            .system(SystemState.initial())
            .input(new LinkedHashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, NOW, SEED, schema.getHash()))
            .build();

        Intent intent = new Intent("missing", Map.of(), "intent-2");
        HostContext context = HostContext.builder(NOW, SEED).durationMs(0L).build();
        return Compute.computeSync(schema, snapshot, intent, context, 5);
    }

    private ComputeResult runEffectPendingCase() throws Exception {
        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash")
            .addAction(new ActionSpec.Builder("fetch")
                .addInputField("id", FieldSpec.required("id", "string"))
                .flow(FlowNode.Effect.of(
                    "api.fetch",
                    Map.of("id", new Get("input.id"))
                ))
                .build())
            .build();

        Snapshot snapshot = Snapshot.builder()
            .data(new LinkedHashMap<>())
            .computed(new LinkedHashMap<>())
            .system(SystemState.initial())
            .input(new LinkedHashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, NOW, SEED, schema.getHash()))
            .build();

        Intent intent = new Intent("fetch", Map.of("id", "abc"), "intent-3");
        HostContext context = HostContext.builder(NOW, SEED).durationMs(0L).build();
        return Compute.computeSync(schema, snapshot, intent, context, 5);
    }

    private ComputeResult runMetaIntentCase() throws Exception {
        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash")
            .addDataField(FieldSpec.required("value", "string"))
            .addAction(new ActionSpec.Builder("markIntent")
                .addInputField("name", FieldSpec.required("name", "string"))
                .flow(FlowNode.Patch.set(
                    "value",
                    new Get("meta.intentId")
                ))
                .build())
            .build();

        Snapshot snapshot = Snapshot.builder()
            .data(new LinkedHashMap<>())
            .computed(new LinkedHashMap<>())
            .system(SystemState.initial())
            .input(new LinkedHashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, NOW, SEED, schema.getHash()))
            .build();

        Intent intent = new Intent("markIntent", Map.of("name", "Alice"), "intent-3");
        HostContext context = HostContext.builder(NOW, SEED).durationMs(0L).build();
        return Compute.computeSync(schema, snapshot, intent, context, 5);
    }

    private Map<String, Object> normalize(ComputeResult result, Map<String, Object> expected) {
        Map<String, Object> out = new LinkedHashMap<>();

        if (expected.containsKey("status")) {
            out.put("status", result.getStatus().name());
        }
        if (expected.containsKey("data")) {
            out.put("data", result.getSnapshot().getData());
        }
        if (expected.containsKey("computed")) {
            out.put("computed", result.getSnapshot().getComputed());
        }
        if (expected.containsKey("input")) {
            out.put("input", result.getSnapshot().getInput());
        }
        if (expected.containsKey("systemStatus")) {
            out.put("systemStatus", result.getSnapshot().getSystem().getStatus().name());
        }
        if (expected.containsKey("currentAction")) {
            out.put("currentAction", result.getSnapshot().getSystem().getCurrentAction());
        }
        if (expected.containsKey("pendingRequirementTypes")) {
            List<String> types = result.getSnapshot().getSystem().getPendingRequirements().stream()
                .map(Requirement::getType)
                .toList();
            out.put("pendingRequirementTypes", types);
        }
        if (expected.containsKey("requirements")) {
            List<Map<String, Object>> reqs = result.getRequirements().stream()
                .map(req -> Map.of(
                    "type", req.getType(),
                    "params", req.getParams()
                ))
                .toList();
            out.put("requirements", reqs);
        }
        if (expected.containsKey("errorCode")) {
            ErrorValue error = result.getSnapshot().getSystem().getLastError();
            out.put("errorCode", error == null ? null : error.getCode());
        }
        if (expected.containsKey("metaVersion")) {
            out.put("metaVersion", result.getSnapshot().getMeta().getVersion());
        }
        if (expected.containsKey("metaTimestamp")) {
            out.put("metaTimestamp", result.getSnapshot().getMeta().getTimestamp());
        }

        return out;
    }

    private void assertJsonEquals(Object expected, Object actual, String message) throws Exception {
        JsonNode expectedNode = mapper.valueToTree(expected);
        JsonNode actualNode = mapper.valueToTree(actual);
        if (!expectedNode.equals(actualNode)) {
            throw new AssertionError(message + "\nExpected: " + expectedNode + "\nActual: " + actualNode);
        }
    }

    private List<Map<String, Object>> loadVectors(String resourcePath) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing resource: " + resourcePath);
            }
            return mapper.readValue(input, new TypeReference<>() {});
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }
}
