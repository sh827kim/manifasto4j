package ai.manifesto.core.core;

import ai.manifesto.core.ExplainResult;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Explain edge-case tests")
class ExplainTest {

    @Test
    void explainsComputedPathWithDependencies() {
        DomainSchema schema = buildSchemaWithComputed();
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("count", 3)))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>(Map.of("n", 2)))
            .meta(Snapshot.SnapshotMeta.create(1, 1000L, "seed", schema.getHash()))
            .build();

        ExplainResult result = Explain.explain(schema, snapshot, "computed.total");
        assertNotNull(result);
        assertEquals(3, result.getValue());
        assertEquals(List.of("count"), result.getDeps());
        assertEquals("computed.total", result.getTrace().getSourcePath());
    }

    @Test
    void fallsBackToStoredComputedValueWhenDefinitionMissing() {
        DomainSchema schema = buildSchemaWithComputed();
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("count", 1)))
            .computed(new HashMap<>(Map.of("computed.unknown", 99)))
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(1, 1000L, "seed", schema.getHash()))
            .build();

        ExplainResult result = Explain.explain(schema, snapshot, "computed.unknown");
        assertEquals(99, result.getValue());
        assertTrue(result.getDeps().isEmpty());
        assertEquals("computed.unknown", result.getTrace().getSourcePath());
    }

    @Test
    void explainsSystemInputAndDataPaths() {
        DomainSchema schema = buildSchemaWithComputed();
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("count", 7)))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>(Map.of("n", 5)))
            .meta(Snapshot.SnapshotMeta.create(1, 1000L, "seed", schema.getHash()))
            .build();

        ExplainResult dataResult = Explain.explain(schema, snapshot, "count");
        assertEquals(7, dataResult.getValue());

        ExplainResult inputResult = Explain.explain(schema, snapshot, "input.n");
        assertEquals(5, inputResult.getValue());

        ExplainResult systemResult = Explain.explain(schema, snapshot, "system.status");
        assertNotNull(systemResult.getValue());
    }

    private DomainSchema buildSchemaWithComputed() {
        DomainSchema.Builder temp = new DomainSchema.Builder("urn:test:explain", "1.0.0")
            .addDataField(FieldSpec.required("count", "number"))
            .addComputedField(new ComputedFieldDef.Builder("computed.total", new Get("count"))
                .addDependency("count")
                .build())
            .addAction(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build());
        DomainSchema tempSchema = temp.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        return new DomainSchema.Builder("urn:test:explain", "1.0.0")
            .hash(hash)
            .addDataField(FieldSpec.required("count", "number"))
            .addComputedField(new ComputedFieldDef.Builder("computed.total", new Get("count"))
                .addDependency("count")
                .build())
            .addAction(new ActionSpec.Builder("noop").flow(FlowNode.Halt.of(null)).build())
            .build();
    }
}
