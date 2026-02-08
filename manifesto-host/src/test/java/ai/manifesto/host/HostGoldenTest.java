package ai.manifesto.host;

import ai.manifesto.core.*;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.comparison.Eq;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Host Golden Tests")
class HostGoldenTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Host 경계 동작이 골든 기대값과 일치")
    void hostGoldenCases() throws Exception {
        List<Map<String, Object>> vectors = loadVectors("golden/host-e2e.json");
        assertFalse(vectors.isEmpty(), "Golden vectors should not be empty");

        for (Map<String, Object> vector : vectors) {
            String name = String.valueOf(vector.get("name"));
            @SuppressWarnings("unchecked")
            Map<String, Object> expected = (Map<String, Object>) vector.get("expected");
            assertNotNull(expected, "Expected golden data missing for: " + name);

            ComputeResult result = switch (name) {
                case "effect-applied-with-host-slot" -> runEffectAppliedCase();
                case "missing-handler-pending" -> runMissingHandlerCase();
                default -> throw new IllegalArgumentException("Unknown host golden case: " + name);
            };

            Map<String, Object> actual = normalize(result, expected);
            assertJsonEquals(expected, actual, "Golden mismatch: " + name);
        }
    }

    private ComputeResult runEffectAppliedCase() throws Exception {
        DomainSchema schema = createNotifySchema("urn:test:host:golden:1");
        Snapshot snapshot = createSnapshot(schema);
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));
        Intent intent = new Intent("notify", Map.of(), "intent-host-golden-1");
        return host.run(schema, snapshot, intent, 5);
    }

    private ComputeResult runMissingHandlerCase() throws Exception {
        DomainSchema schema = createNotifySchema("urn:test:host:golden:2");
        Snapshot snapshot = createSnapshot(schema);
        HostRuntime host = new HostRuntime();
        Intent intent = new Intent("notify", Map.of(), "intent-host-golden-2");
        return host.run(schema, snapshot, intent, 5);
    }

    private DomainSchema createNotifySchema(String schemaId) {
        FlowNode flow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();

        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(schemaId, "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""));
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        return new DomainSchema.Builder(schemaId, "1.0.0")
            .hash(hash)
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""))
            .build();
    }

    private Snapshot createSnapshot(DomainSchema schema) {
        return Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 1L, "seed", schema.getHash()))
            .build();
    }

    private Map<String, Object> normalize(ComputeResult result, Map<String, Object> expected) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (expected.containsKey("status")) {
            out.put("status", result.getStatus().name());
        }
        if (expected.containsKey("dataStatus")) {
            out.put("dataStatus", result.getSnapshot().getData().get("status"));
        }
        if (expected.containsKey("hostCurrentIntentId")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> hostState = (Map<String, Object>) result.getSnapshot().getData().get("$host");
            Object currentIntentId = hostState != null ? hostState.get("currentIntentId") : null;
            out.put("hostCurrentIntentId", currentIntentId);
        }
        if (expected.containsKey("hasRequirements")) {
            out.put("hasRequirements", !result.getRequirements().isEmpty());
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
