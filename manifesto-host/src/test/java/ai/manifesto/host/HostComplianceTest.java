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
import ai.manifesto.host.runtime.HostRuntimeTraceEvent;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HostComplianceTest {

    @Test
    void orderingAndLivenessInvariantsHoldForChainedEffects() throws Exception {
        FlowNode flow = FlowNode.Seq.of(List.of(
            FlowNode.If.of(new ai.manifesto.core.expr.type.IsNull(new Get("step1")), FlowNode.Effect.of("host.step1", Map.of())),
            FlowNode.If.of(new ai.manifesto.core.expr.logical.And(List.of(new Get("step1"), new ai.manifesto.core.expr.type.IsNull(new Get("step2")))), FlowNode.Effect.of("host.step2", Map.of())),
            FlowNode.If.of(new ai.manifesto.core.expr.logical.And(List.of(new Get("step2"), new ai.manifesto.core.expr.type.IsNull(new Get("step3")))), FlowNode.Effect.of("host.step3", Map.of())),
            FlowNode.If.of(new Eq(new Get("step3"), new Lit(true)), FlowNode.Halt.of("done"))
        ));
        ActionSpec action = new ActionSpec.Builder("chain").flow(flow).build();
        DomainSchema schema = buildSchema("urn:test:host:compliance:chain", action,
            new FieldSpec("step1", "boolean", false, null),
            new FieldSpec("step2", "boolean", false, null),
            new FieldSpec("step3", "boolean", false, null)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("step1", null);
        data.put("step2", null);
        data.put("step3", null);
        Snapshot snapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 1L, "seed", schema.getHash()))
            .build();

        List<HostRuntimeTraceEvent> trace = new ArrayList<>();
        HostRuntime host = new HostRuntime()
            .register("host.step1", params -> EffectResult.of(List.of(Patch.set("step1", true))))
            .register("host.step2", params -> EffectResult.of(List.of(Patch.set("step2", true))))
            .register("host.step3", params -> EffectResult.of(List.of(Patch.set("step3", true))));

        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("chain", Map.of(), "intent-chain"),
            HostRuntimeOptions.builder().traceSink(trace::add).build()
        );

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertEquals(true, result.getSnapshot().getData().get("step3"));

        long runnerStart = trace.stream().filter(e -> "runner:start".equals(e.type())).count();
        long runnerEnd = trace.stream().filter(e -> "runner:end".equals(e.type())).count();
        long jobStart = trace.stream().filter(e -> "job:start".equals(e.type())).count();
        long jobEnd = trace.stream().filter(e -> "job:end".equals(e.type())).count();

        assertEquals(runnerStart, runnerEnd);
        assertEquals(jobStart, jobEnd);
        assertTrue(trace.stream().anyMatch(e -> "continue:enqueue".equals(e.type())));
    }

    @Test
    void effectRetryTimeoutAndFailureSignalsAreEmitted() throws Exception {
        FlowNode flow = FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")));
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();
        DomainSchema schema = buildSchema("urn:test:host:compliance:effect", action, new FieldSpec("status", "string", false, ""));
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 1L, "seed", schema.getHash()))
            .build();

        List<HostRuntimeTraceEvent> trace = new ArrayList<>();
        AtomicInteger calls = new AtomicInteger();
        HostRuntime host = new HostRuntime().register("host.notify", params -> {
            calls.incrementAndGet();
            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return EffectResult.of(List.of());
        });

        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("notify", Map.of(), "intent-timeout"),
            HostRuntimeOptions.builder()
                .maxEffectRetries(1)
                .maxEffectDurationMillis(1)
                .traceSink(trace::add)
                .build()
        );

        assertEquals(ComputeStatus.ERROR, result.getStatus());
        assertTrue(calls.get() >= 2);
        assertTrue(trace.stream().anyMatch(e -> "effect:retry".equals(e.type())));
        assertTrue(trace.stream().anyMatch(e -> "effect:failure".equals(e.type())));
    }

    @Test
    void hostNamespaceConsistencyIsMaintained() throws Exception {
        FlowNode flow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();
        DomainSchema schema = buildSchema("urn:test:host:compliance:namespace", action, new FieldSpec("status", "string", false, ""));
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 1L, "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime().register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));
        Intent intent = new Intent("notify", Map.of(), "intent-ns");
        ComputeResult result = host.run(schema, snapshot, intent, 5);

        @SuppressWarnings("unchecked")
        Map<String, Object> hostState = (Map<String, Object>) result.getSnapshot().getData().get("$host");
        assertNotNull(hostState);
        assertEquals("intent-ns", hostState.get("currentIntentId"));

        @SuppressWarnings("unchecked")
        Map<String, Object> slots = (Map<String, Object>) hostState.get("intentSlots");
        assertNotNull(slots);
        @SuppressWarnings("unchecked")
        Map<String, Object> slot = (Map<String, Object>) slots.get("intent-ns");
        assertNotNull(slot);
        assertEquals("notify", slot.get("type"));

        assertTrue(result.getSnapshot().getSystem().getPendingRequirements().isEmpty());
    }

    private DomainSchema buildSchema(String id, ActionSpec action, FieldSpec... fields) {
        DomainSchema.Builder temp = new DomainSchema.Builder(id, "1.0.0").addAction(action);
        for (FieldSpec field : fields) {
            temp.addDataField(field);
        }
        DomainSchema tempSchema = temp.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        DomainSchema.Builder builder = new DomainSchema.Builder(id, "1.0.0").hash(hash).addAction(action);
        for (FieldSpec field : fields) {
            builder.addDataField(field);
        }
        return builder.build();
    }
}
