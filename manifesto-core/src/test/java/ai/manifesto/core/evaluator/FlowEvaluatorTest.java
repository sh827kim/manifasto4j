package ai.manifesto.core.evaluator;

import ai.manifesto.core.*;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.trace.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FlowEvaluator 배열 효과 테스트")
class FlowEvaluatorTest {

    private DomainSchema schema;
    private Snapshot snapshot;
    private EvalContext context;

    @BeforeEach
    void setUp() {
        schema = new DomainSchema.Builder("test-schema", "1.0.0")
            .hash("test-hash")
            .addDataField(new FieldSpec("items", "array", false, List.of()))
            .addDataField(new FieldSpec("mapped", "array", false, List.of()))
            .addDataField(new FieldSpec("filtered", "array", false, List.of()))
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("items", List.of(
            Map.of("name", "a", "done", true),
            Map.of("name", "b", "done", false),
            Map.of("name", "c", "done", true)
        ));

        snapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();

        TraceContext trace = TraceContext.create(System.currentTimeMillis());
        context = EvalContext.builder()
            .snapshot(snapshot)
            .schema(schema)
            .currentAction("testAction")
            .nodePath("actions.test.flow")
            .intentId("intent-1")
            .trace(trace)
            .build();
    }

    @Test
    @DisplayName("array.map 인라인 처리")
    void testArrayMapInlineEffect() {
        FlowNode.Effect effect = FlowNode.Effect.of(
            "array.map",
            Map.of(
                "source", new Get("items"),
                "into", new Lit("mapped"),
                "select", new Get("$item.name")
            )
        );

        FlowResult result = FlowEvaluator.evaluate(
            effect,
            context,
            FlowState.initial(snapshot),
            "actions.test.flow"
        ).join();

        assertEquals(FlowStatus.RUNNING, result.state().getStatus());
        assertTrue(result.state().getRequirements().isEmpty());
        assertEquals(1, result.state().getPatches().size());

        Object mapped = result.state().getSnapshot().getData().get("mapped");
        assertEquals(List.of("a", "b", "c"), mapped);
    }

    @Test
    @DisplayName("array.filter 인라인 처리")
    void testArrayFilterInlineEffect() {
        FlowNode.Effect effect = FlowNode.Effect.of(
            "array.filter",
            Map.of(
                "source", new Get("items"),
                "into", new Lit("filtered"),
                "where", new Get("$item.done")
            )
        );

        FlowResult result = FlowEvaluator.evaluate(
            effect,
            context,
            FlowState.initial(snapshot),
            "actions.test.flow"
        ).join();

        assertEquals(FlowStatus.RUNNING, result.state().getStatus());
        assertTrue(result.state().getRequirements().isEmpty());
        assertEquals(1, result.state().getPatches().size());

        Object filtered = result.state().getSnapshot().getData().get("filtered");
        assertEquals(List.of(
            Map.of("name", "a", "done", true),
            Map.of("name", "c", "done", true)
        ), filtered);
    }
}
